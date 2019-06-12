package com.fullfacing.keycloak4s.auth.akka.http.services

import java.util.{Date, UUID}

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logValidationEx
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.auth.akka.http.services.cache.{JwksCache, JwksDynamicCache, JwksStaticCache}
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.Exceptions.buildClaimsException
import com.fullfacing.keycloak4s.core.models.enums.{TokenType, TokenTypes}
import com.fullfacing.keycloak4s.core.models.{KeycloakConfig, KeycloakException}
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

/**
 * A bearer token validator capable of parsing and validating tokens.
 * Requires JwksStaticCache or JwksDynamicCache (or a custom JwksCache) to be mixed in.
 *
 * JwksStaticCache allows a JWK set to be defined. It does not require connection to the Keycloak server,
 * however the JWK set cannot be modified during runtime.
 *
 * JwksDynamicCache retrieves and caches the JWK set from the Keycloak server. It requires a connection to the Keycloak
 * server, but can dynamically refresh the JWK set if it does not contain a public key required by a token.
 *
 * @param keycloakConfig  A Keycloak configuration containing the Keycloak server details.
 * @param ec              The execution context to be used for parallel token validation. Defaults to the global context.
 */
abstract class TokenValidator(val keycloakConfig: KeycloakConfig)(implicit ec: ExecutionContext = global)
  extends JwksCache with ClaimValidators {

  /**
   * Validates the claims set of a token, specifically the exp, nbf, iat and iss fields.
   * Does not short-circuit, all errors are captured and ultimately converted into one exception.
   */
  private def validateClaims(token: SignedJWT, now: Date): Either[KeycloakException, Unit] = {
    val claims  = token.getJWTClaimsSet
    val uri     = s"${config.scheme}://${config.host}:${config.port}/auth/realms/${config.realm}"

    val validationResults = List(
      validateExp(claims, now),
      validateNbf(claims, now),
      validateIat(claims, now),
      validateIss(claims, uri)
    )

    validationResults
      .combineAll
      .toEither
      .leftMap(buildClaimsException)
  }

  /**
   * Checks if there is a public key in the JWK set cache that matches the key ID in the token header and
   * uses it to verify the token's signature. Reattempts once in case of failure.
   */
  private def validateSignature(token: SignedJWT, jwks: JWKSet, reattempted: Boolean = false)(implicit cId: UUID): IO[Either[KeycloakException, Unit]] = {

    /* Refreshes the cache and recursively re-attempts validateSignature. **/
    def reattemptValidation() = attemptRecache().flatMap { _ =>
      validateSignature(token, jwks, reattempted = true)
    }

    /* Creates a RSA verifier with a RSA key and verifies the bearer token. **/
    def verify(rsaKey: RSAKey): Boolean = token.verify(new RSASSAVerifier(rsaKey))

    Option(jwks.getKeyByKeyId(token.getHeader.getKeyID)) match {
      case None if !reattempted         => reattemptValidation()                            //No matching public key found in cache. Attempt recache.
      case None                         => IO.pure(Exceptions.PUBLIC_KEY_NOT_FOUND.asLeft)  //No matching public key found after recache.
      case Some(k: RSAKey) if verify(k) => IO.pure(().asRight)                              //Public key found, signal verification passed.
      case Some(_)                      => IO.pure(Exceptions.SIG_INVALID.asLeft)           //Public key found, signal verification failed.
    }
  }.handleError(ex => Exceptions.UNEXPECTED(ex.getMessage).asLeft)

  /**
   * Attempts to parse a token.
   */
  private def parseToken(rawToken: String): IO[Either[KeycloakException, SignedJWT]] = IO {
    parse(rawToken).asRight[KeycloakException]
  }.handleError(_ => Exceptions.PARSE_FAILED.asLeft)

  /**
   * Parses a token and passes it through all validators.
   */
  private def executeValidators(rawToken: String, now: Date, tokenType: TokenType)(implicit cId: UUID): IO[Either[KeycloakException, SignedJWT]] = {
    val transformer = for {
      token     <- EitherT(parseToken(rawToken))
      _         <- EitherT.fromEither[IO](validateClaims(token, now))
      keySet    <- EitherT(getCachedValue())
      _         <- EitherT(validateSignature(token, keySet))
    } yield token

    transformer
      .leftMap(logValidationEx(_, tokenType))
      .value
      .handleError(ex => logValidationEx(Exceptions.UNEXPECTED(ex.getMessage), tokenType).asLeft)
  }

  /**
   * Parses and validates an access token.
   */
  def validate(rawToken: String): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val cId: UUID = UUID.randomUUID()
    Logging.tokenValidating(cId)

    executeValidators(rawToken, new Date(), TokenTypes.Access)
      .map(_.map { token =>
        Logging.tokenValidated(cId)
        AuthPayload(token.getPayload)
      })
  }

  /**
   * Parses and validates an access and ID token in parallel.
   */
  def validateParallel(rawAccessToken: String, rawIdToken: String): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val cId: UUID = UUID.randomUUID()
    implicit val context: ContextShift[IO] = IO.contextShift(ec)
    Logging.tokenValidating(cId)

    val now = new Date()
    val io1 = executeValidators(rawAccessToken, now, TokenTypes.Access)
    val io2 = executeValidators(rawIdToken, now, TokenTypes.Id)

    (io1, io2).parMapN {
      case (Left(err), _)           => err.asLeft
      case (_, Left(err))           => err.asLeft
      case (Right(a), Right(i))     => Logging.tokenValidated(cId); AuthPayload(a.getPayload, i.getPayload.some).asRight
    }
  }
}

object TokenValidator {
  /* TokenValidator instantiable with a static cache. **/
  class Static(val jwks: JWKSet, val config: KeycloakConfig) extends TokenValidator(config) with JwksStaticCache

  /* TokenValidator instantiable with a dynamic cache. **/
  class Dynamic(val config: KeycloakConfig) extends TokenValidator(config) with JwksDynamicCache

  /* Apply for TokenValidator.Static **/
  def apply(jwks: JWKSet, config: KeycloakConfig) = new Static(jwks, config)

  /* Apply for TokenValidator.Dynamic **/
  def apply(config: KeycloakConfig) = new Dynamic(config)
}

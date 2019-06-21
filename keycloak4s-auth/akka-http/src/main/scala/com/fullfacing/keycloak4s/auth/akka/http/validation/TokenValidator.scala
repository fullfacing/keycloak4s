package com.fullfacing.keycloak4s.auth.akka.http.validation

import java.util.{Date, UUID}

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.Logging
import com.fullfacing.keycloak4s.auth.akka.http.Logging.logValidationEx
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.auth.akka.http.validation.cache.{JwksCache, JwksDynamicCache, JwksStaticCache}
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
 * A bearer token validator capable of parsing and validating serialized JSON web tokens.
 * Requires JwksStaticCache, JwksDynamicCache, or a custom implementation of the JwksCache to be mixed in for signature validation.
 *
 * JwksDynamicCache retrieves the JWK set from the Keycloak server and caches it. It requires a connection to the Keycloak
 * server, but can dynamically re-retrieve the JWK set if it does not contain a public key required by a token.
 *
 * JwksStaticCache allows for a predefined JWK set. It does not require connection to the Keycloak server, however
 * the JWK set cannot be modified during runtime.
 *
 * @param keycloakConfig  A Keycloak configuration containing the Keycloak server details.
 * @param ec              The execution context to be used for parallel token validation.
 */
abstract class TokenValidator(val keycloakConfig: KeycloakConfig)(implicit ec: ExecutionContext)
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
      case None if !reattempted         => reattemptValidation()                            //No matching public key found in cache. Attempt re-cache.
      case None                         => IO.pure(Exceptions.PUBLIC_KEY_NOT_FOUND.asLeft)  //No matching public key found after re-cache.
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
  private def validate(rawToken: String, now: Date, tokenType: TokenType, keySet: JWKSet)(implicit cId: UUID): IO[Either[KeycloakException, SignedJWT]] = {
    val validationResult = for {
      token     <- EitherT(parseToken(rawToken))
      _         <- EitherT.fromEither[IO](validateClaims(token, now))
      _         <- EitherT(validateSignature(token, keySet))
    } yield token

    validationResult
      .leftMap(logValidationEx(_, tokenType))
      .value
      .handleError(ex => logValidationEx(Exceptions.UNEXPECTED(ex.getMessage), tokenType).asLeft)
  }

  /**
   * Parses and validates an access token.
   */
  def process(rawToken: String)(implicit cId: UUID): IO[Either[KeycloakException, AuthPayload]] = {
    Logging.tokenValidating(cId)

    for {
      keySet  <- EitherT(getCachedValue())
      token   <- EitherT(validate(rawToken, new Date(), TokenTypes.Access, keySet))
    } yield {
      Logging.tokenValidated(cId)
      AuthPayload(token.getPayload)
    }
  }.value

  /**
   * Parses and validates an access and ID token in parallel.
   */
  def parProcess(rawAccessToken: String, rawIdToken: String)(implicit cId: UUID): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val context: ContextShift[IO] = IO.contextShift(ec)
    Logging.tokenValidating(cId)

    val now = new Date()

    EitherT(getCachedValue()).flatMapF { keySet =>
      val io1 = validate(rawAccessToken, now, TokenTypes.Access, keySet)
      val io2 = validate(rawIdToken, now, TokenTypes.Id, keySet)

      (io1, io2).parMapN {
        case (Left(err), _)       => err.asLeft
        case (_, Left(err))       => err.asLeft
        case (Right(a), Right(i)) => Logging.tokenValidated(cId); AuthPayload(a.getPayload, i.getPayload.some).asRight
      }
    }
  }.value
}

object TokenValidator {

  /* TokenValidator instantiable with a static cache. **/
  final class Static(val jwks: JWKSet, val config: KeycloakConfig)(implicit ec: ExecutionContext = global)
    extends TokenValidator(config) with JwksStaticCache

  /* TokenValidator instantiable with a dynamic cache. **/
  final class Dynamic(val config: KeycloakConfig)(implicit ec: ExecutionContext = global)
    extends TokenValidator(config) with JwksDynamicCache

  /* Applies for Static and Dynamic. **/
  object Static {
    def apply(jwks: JWKSet, config: KeycloakConfig)(implicit ec: ExecutionContext = global) = new Static(jwks, config)
  }

  object Dynamic {
    def apply(config: KeycloakConfig)(implicit ec: ExecutionContext = global) = new Dynamic(config)
  }
}

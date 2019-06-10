package com.fullfacing.keycloak4s.auth.akka.http.services

import java.time.Instant
import java.util.UUID

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logValidationEx
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.Exceptions.buildClaimsException
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.fullfacing.keycloak4s.core.models.enums.{TokenType, TokenTypes}
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

/**
 * A bearer token validator capable of parsing and validating either one token or two in parallel.
 * Includes functionality to retrieve the JWKS from the Keycloak server, with caching to avoid unnecessary server calls.
 * Requires URI details of the Keycloak server for both the JWKS retrieval and for ISS checking.
 *
 * @param scheme  The scheme of the Keycloak server's URI.
 * @param host    The host of the Keycloak server's URI.
 * @param port    The port of the Keycloak server's URI.
 * @param realm   The Keycloak Realm the token has access to.
 * @param leeway  Amount of seconds the expiration, not-before and issues-at times are allowed to vary, sometimes required
 *                if significant network lag is expected. Use with caution.
 * @param ec      The execution context to be used for parallel token validation. Defaults to the global context.
 */
class TokenValidator(scheme: String, host: String, port: Int, realm: String, leeway: Long = 0l)(implicit ec: ExecutionContext = global)
  extends VerifierCache(scheme, host, port, realm)
    with ClaimValidators {

  /**
   * Validates the claims set of a token, specifically the exp, nbf, iat and iss fields.
   * Does not short-circuit, all errors are captured and ultimately converted into one exception.
   */
  private def validateClaims(token: SignedJWT, now: Instant): Either[KeycloakException, Unit] = {
    val claims  = token.getJWTClaimsSet
    val uri     = s"$scheme://$host:$port/auth/realms/$realm"

    val validationResults = List(
      validateExp(claims, now, leeway),
      validateNbf(claims, now, leeway),
      validateIat(claims, now, leeway),
      validateIss(claims, uri)
    )

    validationResults
      .combineAll
      .toEither
      .leftMap(buildClaimsException)
  }

  /**
   * Retrieves the RSA verifiers from the cache, re-caches once in case of failure.
   */
  private def fetchVerifier()(implicit cId: UUID): IO[Either[KeycloakException, VerifierMap]] = retrieveCachedValue().flatMap {
    case r @ Right(_) => IO.pure(r)
    case Left(_)      => updateCache()
  }

  /**
   * Retrieves the RSA verifiers from the cache, finds a valid verifier for the token based on its keyId header,
   * and verifies the signature with it. Reattempts once in case of failure.
   */
  private def validateSignature(token: SignedJWT, verifierMap: VerifierMap, reattempted: Boolean = false)(implicit cId: UUID): IO[Either[KeycloakException, Unit]] = {

    /* Refreshes the cache and recursively re-attempts validateSignature. **/
    def reattemptValidation() = updateCache().flatMap { _ =>
      validateSignature(token, verifierMap, reattempted = true)
    }

    verifierMap.get(token.getHeader.getKeyID) match {
      case None if !reattempted       => reattemptValidation()
      case None                       => IO.pure(Exceptions.PUBLIC_KEY_NOT_FOUND.asLeft)
      case Some(v) if token.verify(v) => IO.pure(().asRight)
      case Some(_)                    => IO.pure(Exceptions.SIG_INVALID.asLeft)
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
  private def executeValidators(rawToken: String, now: Instant, tokenType: TokenType)(implicit cId: UUID): IO[Either[KeycloakException, SignedJWT]] = {
    val transformer = for {
      token     <- EitherT(parseToken(rawToken))
      _         <- EitherT.fromEither[IO](validateClaims(token, now))
      keySet    <- EitherT(fetchVerifier())
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

    executeValidators(rawToken, Instant.now(), TokenTypes.Access)
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

    val now = Instant.now()
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
  def apply(scheme: String, host: String, port: Int, realm: String) = new TokenValidator(scheme, host, port, realm)
}

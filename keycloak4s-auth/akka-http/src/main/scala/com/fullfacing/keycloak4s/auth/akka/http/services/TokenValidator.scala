package com.fullfacing.keycloak4s.auth.akka.http.services

import java.time.Instant
import java.util.UUID

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.{logValidationEx, logValidationExStack}
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse

class TokenValidator(host: String, port: String, realm: String) extends JwksCache(host, port, realm) {

  /**
   * Validates if a token is not expired or being used before the nbf (if defined).
   */
  private def validateTime(token: SignedJWT): Either[KeycloakException, SignedJWT] = {
    val now = Instant.now()
    val nbf = Option(token.getJWTClaimsSet.getNotBeforeTime).map(_.toInstant)
    val exp = token.getJWTClaimsSet.getExpirationTime.toInstant

    val nbfCond = nbf.fold(true)(n => n == Instant.EPOCH || now.isAfter(n))
    val expCond = now.isBefore(exp)

    if (nbfCond && expCond) token.asRight
    else if (!nbfCond) Exceptions.NOT_YET_VALID.asLeft
    else Exceptions.EXPIRED.asLeft
  }

  /**
   * Checks the key set cache for valid keys, re-caches once (and only once) if invalid.
   */
  private def checkKeySet()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = retrieveCachedValue().flatMap {
    case r @ Right(_) => IO(r)
    case Left(_)      => updateCache().map(_.left.map(_ => Exceptions.JWKS_SERVER_ERROR))
  }

  /**
   * Attempts to obtain the public key matching the key ID in the token header.
   * Re-caches the key set once (and only once) if the key was not found.
   */
  private def matchPublicKey(keyId: String, keys: JWKSet, reattempted: Boolean = false)(implicit cId: UUID): IO[Either[KeycloakException, RSAKey]] = {
    Option(keys.getKeyByKeyId(keyId)) match {
      case None if !reattempted => updateCache().flatMap(_ => matchPublicKey(keyId, keys, reattempted = true))
      case None                 => IO.pure(Exceptions.PUBLIC_KEY_NOT_FOUND.asLeft)
      case Some(k: RSAKey)      => IO(k.asRight)
    }
  }.handleError(ex => Exceptions.UNEXPECTED(ex.getMessage).asLeft)

  /**
   * Validates the token with the public key obtained from the Keycloak server.
   */
  private def validateSignature(token: SignedJWT, publicKey: RSAKey): Either[KeycloakException, Unit] = {
    val verifier = new RSASSAVerifier(publicKey)
    if (token.verify(verifier)) ().asRight else Exceptions.SIG_INVALID.asLeft
  }

  /**
   * Compares an ID and Access token and checks if the subject and state matches.
   */
  private def compareTokens(access: SignedJWT, id: SignedJWT): Either[KeycloakException, Unit] = {
    val accessJson  = access.getPayload.toJSONObject
    val idJson      = id.getPayload.toJSONObject

    val subMatches    = accessJson.getAsString("sub") == idJson.getAsString("sub")
    val stateMatches  = accessJson.getAsString("session_state") == idJson.getAsString("session_state")

    if (subMatches && stateMatches) ().asRight else Exceptions.ID_TOKEN_MISMATCH.asLeft
  }

  /**
   * Parses an ID token, validates its signature and checks if its states and subject is the same as the bearer token.
   */
  private def validateIdToken(rawIdToken: String, accessToken: SignedJWT, publicKey: RSAKey): IO[Either[KeycloakException, Option[SignedJWT]]] = {
    EitherT(attemptParse(rawIdToken)).subflatMap { idToken =>
      for {
        _ <- compareTokens(accessToken, idToken)
        _ <- validateSignature(idToken, publicKey)
      } yield Some(idToken)
    }
  }.value.handleError(ex => Exceptions.UNEXPECTED(ex.getMessage).asLeft)

  /**
   * Attempts to parse a raw bearer token.
   */
  private def attemptParse(rawToken: String): IO[Either[KeycloakException, SignedJWT]] = IO {
    parse(rawToken).asRight[KeycloakException]
  }.handleError(_ => Exceptions.PARSE_FAILED.asLeft)

  /**
   * Parses a bearer token, validate the token's expiration, nbf and signature, and returns the token payload.
   * Additionally parses, validates and returns an optional ID token.
   */
  def validate(rawToken: String, rawIdToken: Option[String] = None): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val cId: UUID = UUID.randomUUID()
    Logging.tokenValidating(cId)

    lazy val idTokenNull = IO.pure(none[SignedJWT].asRight[KeycloakException])

    (for {
      token   <- EitherT(attemptParse(rawToken))
      _       <- EitherT.fromEither[IO](validateTime(token))
      keys    <- EitherT(checkKeySet())
      key     <- EitherT(matchPublicKey(token.getHeader.getKeyID, keys))
      _       <- EitherT.fromEither[IO](validateSignature(token, key))
      id      <- EitherT(rawIdToken.fold(idTokenNull)(validateIdToken(_, token, key)))
    } yield {
      Logging.tokenValidated(cId)
      AuthPayload(accessToken = token.getPayload, idToken = id.map(_.getPayload))
    }).leftMap(logValidationEx).value.handleError { ex =>
      logValidationExStack(Exceptions.UNEXPECTED(ex.getMessage)).asLeft
    }
  }
}

object TokenValidator {
  def apply(host: String, port: String, realm: String) = new TokenValidator(host, port, realm)
}

package com.fullfacing.keycloak4s.auth.akka.http.services

import java.time.Instant
import java.util.UUID

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logValidationException
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse

class TokenValidator(host: String, port: String, realm: String) extends JwksCache(host, port, realm) {

  /**
   * Checks if the token is not expired and is not being used before the nbf (if defined).
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
  private def validateSignature(token: SignedJWT, publicKey: RSAKey): Either[KeycloakException, SignedJWT] = {
    val verifier = new RSASSAVerifier(publicKey)
    if (token.verify(verifier)) token.asRight else Exceptions.SIG_INVALID.asLeft
  }

  /**
   * Parses an ID token, validates its signature and checks if its states and subject is the same as the bearer token.
   */
  private def parseAndValidateIdToken(rawIdToken: String, publicKey: RSAKey, payload: Payload)
  : IO[Either[KeycloakException, Option[SignedJWT]]] = IO {
    val idToken     = parse(rawIdToken)
    val tokenJson   = payload.toJSONObject
    val idTokenJson = idToken.getPayload.toJSONObject

    val subMatches    = tokenJson.getAsString("sub") == idTokenJson.getAsString("sub")
    val stateMatches  = tokenJson.getAsString("session_state") == idTokenJson.getAsString("session_state")

    def validate() = validateSignature(idToken, publicKey) match {
      case Left(_)  => Exceptions.SIG_INVALID_ID.asLeft
      case Right(r) => Some(r).asRight
    }

    if (subMatches && stateMatches) validate() else Exceptions.ID_TOKEN_MISMATCH.asLeft
  }.handleError(ex => Exceptions.PARSE_FAILED_ID(ex.getMessage).asLeft)

  /**
   * Parses a bearer token, validate the token's expiration, nbf and signature, and returns the token payload.
   * Additionally parses, validates and returns an optional ID token.
   */
  def validate(rawToken: String, rawIdToken: Option[String] = None): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val cId: UUID = UUID.randomUUID()
    Logging.tokenValidating(cId)

    val token = IO {
      validateTime(parse(rawToken))
    }.handleError(_ => Exceptions.PARSE_FAILED.asLeft)

    lazy val idTokenNull = IO.pure(none[SignedJWT].asRight[KeycloakException])

    (for {
      parsed  <- EitherT(token)
      keys    <- EitherT(checkKeySet())
      key     <- EitherT(matchPublicKey(parsed.getHeader.getKeyID, keys))
      _       <- EitherT.fromEither[IO](validateSignature(parsed, key))
      id      <- EitherT(rawIdToken.fold(idTokenNull)(parseAndValidateIdToken(_, key, parsed.getPayload)))
    } yield {
      Logging.tokenValidated(cId)
      AuthPayload(accessToken = parsed.getPayload, idToken = id.map(_.getPayload))
    }).leftMap(logValidationException).value.handleError { ex =>
      logValidationException(Exceptions.UNEXPECTED(ex.getMessage)).asLeft
    }
  }
}

object TokenValidator {
  def apply(host: String, port: String, realm: String) = new TokenValidator(host, port, realm)
}

package com.fullfacing.keycloak4s.adapters.akka.http.services

import java.time.Instant

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.adapters.akka.http.Errors
import com.fullfacing.keycloak4s.adapters.akka.http.IoImplicits._
import com.fullfacing.keycloak4s.adapters.akka.http.Logging.logger
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse

import scala.concurrent.ExecutionContext
import scala.util.Try

class TokenValidator(host: String, port: String, realm: String)(implicit ec: ExecutionContext) extends JwksCache(host, port, realm) {

  /**
   * Checks if the token is not expired and is not being used before the nbf (if defined).
   */
  private def validateTime(token: SignedJWT): Either[Throwable, SignedJWT] = {
    val now = Instant.now()
    val nbf = token.getJWTClaimsSet.getNotBeforeTime.toInstant
    val exp = token.getJWTClaimsSet.getExpirationTime.toInstant

    val nbfCond = nbf == Instant.EPOCH || now.isAfter(nbf)
    val expCond = now.isBefore(exp)

    if (nbfCond && expCond) token.asRight
    else if (!nbfCond) Errors.NOT_YET_VALID.asLeft
    else Errors.EXPIRED.asLeft
  }

  /**
   * Checks the key set cache for valid keys, re-caches once (and only once) if invalid.
   */
  private def checkKeySet(): IO[Either[Throwable, JWKSet]] = retrieveCachedValue().flatMap {
    case r @ Right(_) => IO(r)
    case Left(_)      => updateCache().map(_.left.map(_ => Errors.JWKS_SERVER_ERROR))
  }

  /**
   * Attempts to obtain the public key matching the key ID in the token header.
   * Re-caches the key set once (and only once) if the key was not found.
   */
  private def matchPublicKey(keyId: String, keys: JWKSet, reattempted: Boolean = false): IO[Either[Throwable, RSAKey]] = {
    Option(keys.getKeyByKeyId(keyId)) match {
      case None if !reattempted => updateCache().flatMap(_ => matchPublicKey(keyId, keys, reattempted = true))
      case None                 => IO.pure(Errors.PUBLIC_KEY_NOT_FOUND.asLeft)
      case Some(k: RSAKey)      => IO(k.asRight)
    }
  }.handleErrorWithLogging(_ => Errors.UNEXPECTED.asLeft)

  /**
   * Validates the token with the public key obtained from the Keycloak server.
   */
  private def validateSignature(token: SignedJWT, publicKey: RSAKey): Either[Throwable, Unit] = {
    val verifier = new RSASSAVerifier(publicKey)
    if (token.verify(verifier)) ().asRight else Errors.SIG_INVALID.asLeft
  }

  /**
   * Parses a bearer token, validate the token's expiration, nbf and signature, and returns the token payload.
   */
  def validate(rawToken: String): IO[Either[Throwable, Payload]] = {
    val token = Try {
      parse(rawToken)
    }.fold(_ => Errors.PARSE_FAILED.asLeft, validateTime)

    for {
      t     <- EitherT.fromEither[IO](token)
      keys  <- EitherT(checkKeySet())
      key   <- EitherT(matchPublicKey(t.getHeader.getKeyID, keys))
      _     <- EitherT.fromEither[IO](validateSignature(t, key))
    } yield t.getPayload
  }.value.handleErrorWithLogging(_ => Errors.UNEXPECTED.asLeft)
}

object TokenValidator {
  def apply(host: String, port: String, realm: String)(implicit ec: ExecutionContext) = new TokenValidator(host, port, realm)
}

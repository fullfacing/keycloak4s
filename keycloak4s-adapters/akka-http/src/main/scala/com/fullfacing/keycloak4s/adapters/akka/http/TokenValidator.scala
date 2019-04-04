package com.fullfacing.keycloak4s.adapters.akka.http

import java.time.Instant

import cats.implicits._
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.SignedJWT.parse
import org.json4s.Formats

import scala.util.control.NonFatal

object TokenValidator {

  implicit val formats: Formats = org.json4s.DefaultFormats

  /**
   * Checks if the token is not expired and is not being used before the nbf (if defined).
   */
  private def validateTime(exp: Instant, nbf: Instant): Either[Throwable, Unit] = {
    val now = Instant.now()

    val nbfCond = nbf == Instant.EPOCH || now.isAfter(nbf)
    val expCond = now.isBefore(exp)

    if (nbfCond && expCond) ().asRight
    else if (!nbfCond) Errors.NOT_YET_VALID.asLeft
    else Errors.EXPIRED.asLeft
  }

  /**
   * Checks the token signature validation using the public keys pulled from the Keycloak server.
   */
  def validateSignature(token: SignedJWT, keys: List[RSAKey]): Either[Throwable, Unit] = {
    val publicKey = keys.find(x => x.getKeyID  == token.getHeader.getKeyID)

    publicKey.fold(Errors.PUBLIC_KEY_NOT_FOUND.asLeft[Unit]) { key =>
      val verifier = new RSASSAVerifier(key)
      if (token.verify(verifier)) ().asRight else Errors.SIG_INVALID.asLeft
    }
  }

  /**
   * Parses a bearer token, validate the token's expiration, nbf and signature, and returns the token payload.
   */
  def validate(rawToken: String)(implicit publicKeys: List[RSAKey]): Either[Throwable, Payload] = {
    val token = parse(rawToken)
    val nbf = token.getJWTClaimsSet.getNotBeforeTime.toInstant
    val exp = token.getJWTClaimsSet.getExpirationTime.toInstant

    val validator = for {
      _ <- validateSignature(token, publicKeys)
      _ <- validateTime(exp, nbf)
    } yield token.getPayload

    try validator catch { case NonFatal(e) => e.asLeft }
  }
}

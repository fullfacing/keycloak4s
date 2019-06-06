package com.fullfacing.keycloak4s.auth.akka.http.services

import java.util.{Date, UUID}

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logValidationEx
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWK, JWKSet, RSAKey}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import com.nimbusds.jwt.SignedJWT.parse

class TokenValidator(host: String, port: String, realm: String) extends JwksCache(host, port, realm) {

  def validateExp(claims: JWTClaimsSet)

  /**
   * Validates the expiration and not-before dates of an access token (and optionally an ID token).
   */
  def validateClaims(aToken: SignedJWT, idToken: Option[SignedJWT]): Either[KeycloakException, Unit] = {
    val now = new Date()

    def evaluate(token: SignedJWT): Either[KeycloakException, Unit] = {
      val claims = token.getJWTClaimsSet
      val nbf = Option(claims.getNotBeforeTime)
      val exp = claims.getExpirationTime

      val expCond = now.compareTo(exp) < 0
      val nbfCond = nbf.fold(true)(n => n == new Date(0) || now.compareTo(n) > 0)

      if (nbfCond && expCond) ().asRight
      else if (!nbfCond) Exceptions.NOT_YET_VALID.asLeft
      else Exceptions.EXPIRED.asLeft
    }

    evaluate(aToken)
      .flatMap(_ => idToken.fold(().asRight[KeycloakException])(evaluate))
  }

  /**
   * Checks the key set cache for valid keys, re-caches once (and only once) if invalid.
   */
  def checkKeySet()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = retrieveCachedValue().flatMap {
    case r @ Right(_) => IO.pure(r)
    case Left(_)      => updateCache()
  }

  /**
   * Creates an RSASSA verifier with a public RSA key matching the access token's key ID header.
   * Re-caches the key set once (and only once) if the key was not found.
   */
  def createRsaVerifier(keyId: String, keySet: JWKSet, reattempted: Boolean = false)(implicit cId: UUID): IO[Either[KeycloakException, RSASSAVerifier]] = {
    Option(keySet.getKeyByKeyId(keyId)) match {
      case None if !reattempted => updateCache().flatMap(_ => createRsaVerifier(keyId, keySet, reattempted = true))
      case None                 => IO.pure(Exceptions.PUBLIC_KEY_NOT_FOUND.asLeft)
      case Some(k: RSAKey)      => IO.pure(new RSASSAVerifier(k).asRight)
    }
  }.handleError(ex => Exceptions.UNEXPECTED(ex.getMessage).asLeft)

  /**
   * Validates the signature of an access token (and optionally an ID token) using a RSASSA verifier
   * created with a public RSA key received from the Keycloak server.
   */
  def validateSignatures(verifier: RSASSAVerifier, token: SignedJWT, idToken: Option[SignedJWT]): Either[KeycloakException, Unit] = {
    (token, idToken) match {
      case (a, Some(i)) =>
        if (a.verify(verifier) && i.verify(verifier)) ().asRight else Exceptions.SIG_INVALID.asLeft
      case (a, None) =>
        if (a.verify(verifier)) ().asRight else Exceptions.SIG_INVALID.asLeft
    }
  }

  /**
   * Attempts to parse a raw access token (and optionally a raw ID token).
   */
  def parseTokens(rawAccessToken: String, rawIdToken: Option[String]): IO[Either[KeycloakException, (SignedJWT, Option[SignedJWT])]] = IO {
    val accessToken = parse(rawAccessToken)
    val idToken     = rawIdToken.map(parse)
    (accessToken, idToken).asRight[KeycloakException]
  }.handleError(_ => Exceptions.PARSE_FAILED.asLeft)


  def validate(rawToken: String, rawIdToken: Option[String] = None): IO[Either[KeycloakException, AuthPayload]] = {
    implicit val cId: UUID = UUID.randomUUID()
    Logging.tokenValidating(cId)

    (for {
      tokens            <- EitherT(parseTokens(rawToken, rawIdToken))
      (aToken, iToken)  = tokens
      _                 <- EitherT.fromEither[IO](validateClaims(aToken, iToken))
      keySet            <- EitherT(checkKeySet())
      verifier          <- EitherT(createRsaVerifier(aToken.getHeader.getKeyID, keySet))
      _                 <- EitherT.fromEither[IO](validateSignatures(verifier, aToken, iToken))
    } yield {
      Logging.tokenValidated(cId)
      AuthPayload(accessToken = aToken.getPayload, idToken = iToken.map(_.getPayload))
    }).leftMap(logValidationEx).value.handleError { ex =>
      logValidationEx(Exceptions.UNEXPECTED(ex.getMessage)).asLeft
    }
  }
}

object TokenValidator {
  def apply(host: String, port: String, realm: String) = new TokenValidator(host, port, realm)
}

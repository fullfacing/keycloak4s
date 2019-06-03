package com.fullfacing.keycloak4s.auth.tests

import java.time.Instant
import java.util.UUID

import cats.effect.IO
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator
import com.fullfacing.keycloak4s.core.Exceptions
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import org.scalatest.{AsyncFlatSpec, Matchers, PrivateMethodTester}

class ValidationTestsAsync extends AsyncFlatSpec with Matchers with PrivateMethodTester {

  val validator = TokenValidator("","","")
  val cId: UUID = UUID.randomUUID()

  "matchPublicKey" should "successfully return a RSAKey from a JWKSet if the bearer token's KeyID matches a key in the set" in {
    val matchPublicKey = PrivateMethod[IO[Either[Throwable, RSAKey]]]('matchPublicKey)

    val (token, _, publicKey, keySet) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    (validator invokePrivate matchPublicKey(token.getHeader.getKeyID, keySet, true, cId))
      .unsafeToFuture().map(x => assert(x.map(_.toJSONObject) == Right(publicKey.toJSONObject)))
  }

  it should "fail to return a RSAKey from a JWKSet if the bearer token's KeyID does not match a key in the set" in {
    val matchPublicKey = PrivateMethod[IO[Either[Throwable, RSAKey]]]('matchPublicKey)

    val (_, _, _, keySet) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    (validator invokePrivate matchPublicKey("567890", keySet, true, cId))
      .unsafeToFuture().map(x => assert(x == Left(Exceptions.PUBLIC_KEY_NOT_FOUND)))
  }

  "parseAndValidateIdToken" should "successfully parse, validate and return a valid ID Token" in {
    val parseAndValidateIdToken = PrivateMethod[IO[Either[Throwable, SignedJWT]]]('parseAndValidateIdToken)

    val (token, idToken, key, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    (validator invokePrivate parseAndValidateIdToken(idToken.serialize(), key, token.getPayload))
      .unsafeToFuture().map(x => assert(x.isRight))
  }

  it should "fail to validate an ID token's signature with the wrong RSA key" in {
    val parseAndValidateIdToken = PrivateMethod[IO[Either[Throwable, SignedJWT]]]('parseAndValidateIdToken)

    val (token, idToken, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    val key = new RSAKeyGenerator(2048)
      .keyID("12345")
      .generate()
      .toPublicJWK

    (validator invokePrivate parseAndValidateIdToken(idToken.serialize(), key, token.getPayload))
      .unsafeToFuture().map(x => assert(x == Left(Exceptions.SIG_INVALID)))
  }

  it should "fail to validate an ID token if its subject does not match the subject of the bearer token" in {
    val parseAndValidateIdToken = PrivateMethod[IO[Either[Throwable, SignedJWT]]]('parseAndValidateIdToken)

    val (token, idToken, key, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    val payloadJson = token.getPayload.toJSONObject
    payloadJson.replace("sub", "wrong_subject")
    val newPayload = new Payload(payloadJson)

    (validator invokePrivate parseAndValidateIdToken(idToken.serialize(), key, newPayload))
      .unsafeToFuture().map(x => assert(x == Left(Exceptions.ID_TOKEN_MISMATCH)))
  }

  it should "fail to validate an ID token if its state does not match the state of the bearer token" in {
    val parseAndValidateIdToken = PrivateMethod[IO[Either[Throwable, SignedJWT]]]('parseAndValidateIdToken)

    val (token, idToken, key, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    val payloadJson = token.getPayload.toJSONObject
    payloadJson.replace("session_state", "wrong_state")
    val newPayload = new Payload(payloadJson)

    (validator invokePrivate parseAndValidateIdToken(idToken.serialize(), key, newPayload))
      .unsafeToFuture().map(x => assert(x == Left(Exceptions.ID_TOKEN_MISMATCH)))
  }
}


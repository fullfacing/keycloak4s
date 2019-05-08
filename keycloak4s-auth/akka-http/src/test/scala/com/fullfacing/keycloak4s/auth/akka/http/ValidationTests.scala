package com.fullfacing.keycloak4s.auth.akka.http

import java.time.Instant

import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator
import com.fullfacing.keycloak4s.core.Exceptions
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class ValidationTests extends FlatSpec with Matchers with PrivateMethodTester {

  val validator = TokenValidator("","","")

  "validateTime" should "successfully validate an unexpired token used after its NBF" in {
    val validateTime = PrivateMethod[Either[Throwable, SignedJWT]]('validateTime)

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    validator invokePrivate validateTime(token) shouldBe Right(token)
  }

  it should "successfully validate an unexpired token with an epoch NBF" in {
    val validateTime = PrivateMethod[Either[Throwable, SignedJWT]]('validateTime)

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.ofEpochMilli(0L)))

    validator invokePrivate validateTime(token) shouldBe Right(token)
  }

  it should "successfully validate an unexpired token without an NBF" in {
    val validateTime = PrivateMethod[Either[Throwable, SignedJWT]]('validateTime)

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5))

    validator invokePrivate validateTime(token) shouldBe Right(token)
  }

  it should "fail to validate an expired token" in {
    val validateTime = PrivateMethod[Either[Throwable, SignedJWT]]('validateTime)

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().minusSeconds(60 * 5), None)

    validator invokePrivate validateTime(token) shouldBe Left(Exceptions.EXPIRED)
  }

  it should "fail to validate a token used before its NBF" in {
    val validateTime = PrivateMethod[Either[Throwable, SignedJWT]]('validateTime)

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now().plusSeconds(60 * 5)))

    validator invokePrivate validateTime(token) shouldBe Left(Exceptions.NOT_YET_VALID)
  }

  "validateSignature" should "successfully validate a signature with the correct public key" in {
    val validateSignature = PrivateMethod[Either[Throwable, Unit]]('validateSignature)

    val (token, _, publicKey, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    validator invokePrivate validateSignature(token, publicKey) shouldBe Right(token)
  }

  it should "fail to validate a signature with an incorrect public key" in {
    val validateSignature = PrivateMethod[Either[Throwable, Unit]]('validateSignature)

    val publicKey = new RSAKeyGenerator(2048)
      .keyID("12345")
      .generate()
      .toPublicJWK

    val (token, _, _, _) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    validator invokePrivate validateSignature(token, publicKey) shouldBe Left(Exceptions.SIG_INVALID)
  }
}

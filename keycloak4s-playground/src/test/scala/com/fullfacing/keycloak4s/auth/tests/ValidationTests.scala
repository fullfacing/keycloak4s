package com.fullfacing.keycloak4s.auth.tests

import java.time.Instant
import java.util.{Date, UUID}

import cats.data.NonEmptyList
import cats.data.Validated.{invalidNel, valid}
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.services.{ClaimValidators, TokenValidator}
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class ValidationTests extends FlatSpec with Matchers with PrivateMethodTester with ClaimValidators {

  val scheme  = "http"
  val host    = "localhost"
  val port    = 8080
  val realm   = "test"

  val validator     = TokenValidator(scheme, host, port, realm)
  val validatorUri  = s"$scheme://$host:$port/auth/realms/$realm"

  "validateExp" should "successfully validate an unexpired token" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestTokenGenerator.generate(exp)

    validateExp(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate an expired token" in {
    val now = Instant.now()
    val exp = now.minusSeconds(60)

    val token = TestTokenGenerator.generate(exp)

    validateExp(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.EXPIRED)
  }

  "validateNbf" should "successfully validate a token used after its not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)

    val token = TestTokenGenerator.generate(exp, withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate a token used before its not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.plusSeconds(60)

    val token = TestTokenGenerator.generate(exp, withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.NOT_YET_VALID)
  }

  it should "successfully validate a token with a not-before date set to the epoch" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = Instant.ofEpochMilli(0L)

    val token = TestTokenGenerator.generate(exp, withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "successfully validate a token without a not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestTokenGenerator.generate(exp)

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  "validateIat" should "successfully validate a token used after its issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val iat = now.minusSeconds(60)

    val token = TestTokenGenerator.generate(exp, withIat = iat.some)

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate a token used before its issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val iat = now.plusSeconds(60)

    val token = TestTokenGenerator.generate(exp, withIat = iat.some)

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.IAT_INCORRECT)
  }

  it should "fail to validate a token without an issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestTokenGenerator.generate(exp)

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.IAT_MISSING)
  }

  "validateIss" should "successfully validate a token with an issuer that matches the validator's web address details" in {
    val exp = Instant.now()
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestTokenGenerator.generate(exp, withIss = iss.some)

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe valid(())
  }

  it should "fail to validate a token with an issuer that does not match the validator's web address details" in {
    val exp = Instant.now()
    val iss = "http://localhost:9000/auth/realms/test"

    val token = TestTokenGenerator.generate(exp, withIss = iss.some)

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe invalidNel(Exceptions.ISS_INCORRECT)
  }

  it should "fail to validate a token without an issuer" in {
    val exp = Instant.now()

    val token = TestTokenGenerator.generate(exp)

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe invalidNel(Exceptions.ISS_MISSING)
  }

  "validateClaims" should "successfully validate a token if all claims are correct" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestTokenGenerator.generate(exp, withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    val validateClaims = PrivateMethod[Either[KeycloakException, Unit]]('validateClaims)

    validator invokePrivate validateClaims(token, Date.from(now)) shouldBe Right(())
  }

  it should "fail to validate a token if any of the claims are incorrect" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestTokenGenerator.generate(exp, withNbf = nbf.some, withIat = iat.some)
    val token2 = TestTokenGenerator.generate(exp, withNbf = nbf.plusSeconds(120).some, withIat = iat.plusSeconds(120).some, withIss = iss.some)
    val token3 = TestTokenGenerator.generate(exp.minusSeconds(120), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    val ex1 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.ISS_MISSING, Nil))
    val ex2 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.NOT_YET_VALID, Exceptions.IAT_INCORRECT :: Nil))
    val ex3 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.EXPIRED, Nil))

    val validateClaims = PrivateMethod[Either[KeycloakException, Unit]]('validateClaims)

    validator invokePrivate validateClaims(token1, Date.from(now)) shouldBe Left(ex1)
    validator invokePrivate validateClaims(token2, Date.from(now)) shouldBe Left(ex2)
    validator invokePrivate validateClaims(token3, Date.from(now)) shouldBe Left(ex3)
  }

  "parseToken" should "successfully parse a valid token" in {
    val rawToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    val parseToken = PrivateMethod[IO[Either[KeycloakException, SignedJWT]]]('parseToken)

    (validator invokePrivate parseToken(rawToken)).unsafeRunSync() shouldBe a [Right[_,_]]
  }

  it should "fail to parse a malformed token" in {
    val rawToken = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    val parseToken = PrivateMethod[IO[Either[KeycloakException, SignedJWT]]]('parseToken)

    (validator invokePrivate parseToken(rawToken)).unsafeRunSync() shouldBe Left(Exceptions.PARSE_FAILED)
  }

  "validateSignature" should "successfully validate a token with a valid signature" in {
    val token = TestTokenGenerator.generate(Instant.now())

    val verifier = TestTokenGenerator.verifierMap1
    val correlationId   = UUID.randomUUID()

    val validateSignature = PrivateMethod[IO[Either[KeycloakException, Unit]]]('validateSignature)

    (validator invokePrivate validateSignature(token, verifier, true, correlationId))
      .unsafeRunSync() shouldBe Right(())
  }

  it should "fail to validate a token if there is no verifier with its public key ID" in {
    val token = TestTokenGenerator.generate(Instant.now())

    val verifier = TestTokenGenerator.verifierMap2
    val correlationId = UUID.randomUUID()

    val validateSignature = PrivateMethod[IO[Either[KeycloakException, Unit]]]('validateSignature)

    (validator invokePrivate validateSignature(token, verifier, true, correlationId))
      .unsafeRunSync() shouldBe Left(Exceptions.PUBLIC_KEY_NOT_FOUND)
  }

  it should "fail to validate a token with an invalid signature" in {
    val rsaJwk: RSAKey = new RSAKeyGenerator(2048)
      .keyID("12345")
      .generate()

    val signer: JWSSigner = new RSASSASigner(rsaJwk)

    val token = TestTokenGenerator.generate(Instant.now(), signerOverride = signer.some)

    val verifier = TestTokenGenerator.verifierMap1
    val correlationId = UUID.randomUUID()

    val validateSignature = PrivateMethod[IO[Either[KeycloakException, Unit]]]('validateSignature)

    (validator invokePrivate validateSignature(token, verifier, true, correlationId))
      .unsafeRunSync() shouldBe Left(Exceptions.SIG_INVALID)
  }
}


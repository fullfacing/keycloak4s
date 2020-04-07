import java.time.Instant
import java.util.{Date, UUID}

import cats.data.NonEmptyList
import cats.data.Validated.{invalidNel, valid}
import cats.implicits._
import com.fullfacing.keycloak4s.auth.core.validation.{ClaimValidators, TokenValidator}
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.{ConfigWithoutAuth, KeycloakConfig}
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.TestData

class ValidationTests extends AnyFlatSpec with Matchers with PrivateMethodTester with ClaimValidators {

  val scheme  = "http"
  val host    = "localhost"
  val port    = 8080
  val realm   = "test"

  val config: KeycloakConfig = ConfigWithoutAuth(scheme, host, port, realm)

  val validator: TokenValidator = TokenValidator.Static(TestData.jwkSet, config)
  val validatorUri = s"$scheme://$host:$port/auth/realms/$realm"

  implicit val cId: UUID = UUID.randomUUID()

  "validateExp" should "successfully validate an unexpired token" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestData.createToken(Some(exp))

    validateExp(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate an expired token" in {
    val now = Instant.now()
    val exp = now.minusSeconds(60)

    val token = TestData.createToken(Some(exp))

    validateExp(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.EXPIRED)
  }

  it should "fail to validate a token without an expiration date" in {
    val now = Instant.now()

    val token = TestData.createToken()

    validateExp(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.EXP_MISSING)
  }

  "validateNbf" should "successfully validate a token used after its not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)

    val token = TestData.createToken(Some(exp), withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate a token used before its not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.plusSeconds(60)

    val token = TestData.createToken(Some(exp), withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.NOT_YET_VALID)
  }

  it should "successfully validate a token with a not-before date set to the epoch" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = Instant.ofEpochMilli(0L)

    val token = TestData.createToken(Some(exp), withNbf = Some(nbf))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "successfully validate a token without a not-before date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestData.createToken(Some(exp))

    validateNbf(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  "validateIat" should "successfully validate a token used after its issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val iat = now.minusSeconds(60)

    val token = TestData.createToken(Some(exp), withIat = iat.some)

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe valid(())
  }

  it should "fail to validate a token used before its issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val iat = now.plusSeconds(60)

    val token = TestData.createToken(Some(exp), withIat = iat.some)

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.IAT_INCORRECT)
  }

  it should "fail to validate a token without an issued at date" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)

    val token = TestData.createToken(Some(exp))

    validateIat(token.getJWTClaimsSet, Date.from(now)) shouldBe invalidNel(Exceptions.IAT_MISSING)
  }

  "validateIss" should "successfully validate a token with an issuer that matches the validator's web address details" in {
    val exp = Instant.now()
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestData.createToken(Some(exp), withIss = iss.some)

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe valid(())
  }

  it should "fail to validate a token with an issuer that does not match the validator's web address details" in {
    val exp = Instant.now()
    val iss = "http://localhost:9000/auth/realms/test"

    val token = TestData.createToken(Some(exp), withIss = iss.some)

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe invalidNel(Exceptions.ISS_INCORRECT)
  }

  it should "fail to validate a token without an issuer" in {
    val exp = Instant.now()

    val token = TestData.createToken(Some(exp))

    validateIss(token.getJWTClaimsSet, validatorUri) shouldBe invalidNel(Exceptions.ISS_MISSING)
  }

  "validate" should "successfully validate a token if the signature and all claims pass" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    validator.process(token.serialize()).unsafeRunSync() shouldBe a[Right[_, _]]
  }

  it should "fail to validate a token if any of the claims are incorrect" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some)
    val token2 = TestData.createToken(Some(exp), withNbf = nbf.plusSeconds(120).some, withIat = iat.plusSeconds(120).some, withIss = iss.some)
    val token3 = TestData.createToken(Some(exp.minusSeconds(120)), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    val ex1 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.ISS_MISSING, Nil))
    val ex2 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.NOT_YET_VALID, Exceptions.IAT_INCORRECT :: Nil))
    val ex3 = Exceptions.buildClaimsException(NonEmptyList(Exceptions.EXPIRED, Nil))

    validator.process(token1.serialize()).unsafeRunSync() shouldBe Left(ex1)
    validator.process(token2.serialize()).unsafeRunSync() shouldBe Left(ex2)
    validator.process(token3.serialize()).unsafeRunSync() shouldBe Left(ex3)
  }

  it should "fail to parse a malformed token" in {
    val rawToken = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    validator.process(rawToken).unsafeRunSync() shouldBe Left(Exceptions.PARSE_FAILED)
  }

  it should "fail to validate a token if there is no public key in the cache matching the token's keyId header" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, keyIdOverride = Some("does not exist"))

    validator.process(token.serialize()).unsafeRunSync() shouldBe Left(Exceptions.PUBLIC_KEY_NOT_FOUND)
  }

  it should "fail to validate a token with an invalid signature" in {
    val rsaJwk: RSAKey = new RSAKeyGenerator(2048)
      .keyID("12345")
      .generate()

    val signer: JWSSigner = new RSASSASigner(rsaJwk)

    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, signerOverride = signer.some)

    validator.process(token.serialize()).unsafeRunSync() shouldBe Left(Exceptions.SIG_INVALID)
  }

  "validateParallel" should "successfully parse and validate two valid tokens" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)
    val token2 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    validator.parProcess(token1.serialize(), token2.serialize()).unsafeRunSync() shouldBe a[Right[_, _]]
  }

  it should "fail if the first token is invalid" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, keyIdOverride = Some("does not exist"))
    val token2 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)

    validator.parProcess(token1.serialize(), token2.serialize()).unsafeRunSync() shouldBe Left(Exceptions.PUBLIC_KEY_NOT_FOUND)
  }

  it should "fail if the second token is invalid" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some)
    val token2 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, keyIdOverride = Some("does not exist"))

    validator.parProcess(token1.serialize(), token2.serialize()).unsafeRunSync() shouldBe Left(Exceptions.PUBLIC_KEY_NOT_FOUND)
  }

  it should "fail if both tokens are invalid" in {
    val now = Instant.now()
    val exp = now.plusSeconds(60)
    val nbf = now.minusSeconds(60)
    val iat = now.minusSeconds(60)
    val iss = "http://localhost:8080/auth/realms/test"

    val token1 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, keyIdOverride = Some("does not exist"))
    val token2 = TestData.createToken(Some(exp), withNbf = nbf.some, withIat = iat.some, withIss = iss.some, keyIdOverride = Some("does not exist"))

    validator.parProcess(token1.serialize(), token2.serialize()).unsafeRunSync() shouldBe Left(Exceptions.PUBLIC_KEY_NOT_FOUND)
  }
}


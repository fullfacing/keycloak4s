package com.fullfacing.keycloak4s.adapters.akka.http

import java.time.Instant

import cats.effect.IO
import com.fullfacing.keycloak4s.adapters.akka.http.services.TokenValidator
import com.nimbusds.jose.jwk.RSAKey
import org.scalatest.{AsyncFlatSpec, Matchers, PrivateMethodTester}

class ValidationTestsAsync extends AsyncFlatSpec with Matchers with PrivateMethodTester {

  val validator = TokenValidator("","","")

  "matchPublicKey" should "successfully return a RSAKey from a JWKSet if the bearer token's KeyID matches a key in the set" in {
    val validateFunction = PrivateMethod[IO[Either[Throwable, RSAKey]]]('matchPublicKey)

    val (token, publicKey, keySet) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    (validator invokePrivate validateFunction(token.getHeader.getKeyID, keySet, true))
      .unsafeToFuture().map(x => assert(x.map(_.toJSONObject) == Right(publicKey.toJSONObject)))
  }

  it should "fail to return a RSAKey from a JWKSet if the bearer token's KeyID does not match a key in the set" in {
    val validateFunction = PrivateMethod[IO[Either[Throwable, RSAKey]]]('matchPublicKey)

    val (_, _, keySet) = TestTokenGenerator.generateData(Instant.now().plusSeconds(60 * 5), Some(Instant.now()))

    (validator invokePrivate validateFunction("567890", keySet, true))
      .unsafeToFuture().map(x => assert(x == Left(Errors.PUBLIC_KEY_NOT_FOUND)))
  }
}

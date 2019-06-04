package com.fullfacing.keycloak4s.auth.tests

import java.time.Instant
import java.util.{Date, UUID}

import com.nimbusds.jose._
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jose.util.JSONObjectUtils
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}

object TestTokenGenerator {

  def generateData(withExp: Instant, withNbf: Option[Instant] = None): (SignedJWT, SignedJWT, RSAKey, JWKSet) = {

    val claimsSetBuilder = new JWTClaimsSet.Builder()
      .expirationTime(Date.from(withExp))
      .issuer("http://localhost:8080/unit/test")
      .jwtID(UUID.randomUUID().toString)
      .issueTime(Date.from(withExp))
      .subject("test_subject")
      .claim("session_state", "test_state")

    val idClaimsSetBuilder = new JWTClaimsSet.Builder()
      .expirationTime(Date.from(withExp))
      .issuer("http://localhost:8080/unit/test")
      .jwtID(UUID.randomUUID().toString)
      .issueTime(Date.from(withExp))
      .subject("test_subject")
      .claim("session_state", "test_state")

    val claimsSet = withNbf.fold(claimsSetBuilder.build()) { nbf =>
      claimsSetBuilder.notBeforeTime(Date.from(nbf)).build()
    }

    val idClaimsSet = withNbf.fold(idClaimsSetBuilder.build()) { nbf =>
      idClaimsSetBuilder.notBeforeTime(Date.from(nbf)).build()
    }

    val rsaJwk = new RSAKeyGenerator(2048)
      .keyID("12345")
      .generate()

    val publicKey = rsaJwk.toPublicJWK

    val obj = JSONObjectUtils.parse(s"""{"keys": [${publicKey.toJSONObject}]}""")
    val jwkSet = JWKSet.parse(obj)

    val signer: JWSSigner = new RSASSASigner(rsaJwk)

    val header: JWSHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
      .keyID(rsaJwk.getKeyID)
      .build()

    val jwt: SignedJWT = new SignedJWT(header, claimsSet)
    val idToken: SignedJWT = new SignedJWT(header, idClaimsSet)

    jwt.sign(signer)
    idToken.sign(signer)

    (jwt, idToken, publicKey, jwkSet)
  }
}
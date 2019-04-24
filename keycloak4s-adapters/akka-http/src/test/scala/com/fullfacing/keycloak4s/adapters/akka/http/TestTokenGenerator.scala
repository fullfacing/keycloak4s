package com.fullfacing.keycloak4s.adapters.akka.http

import java.time.Instant
import java.util.{Date, UUID}

import com.nimbusds.jose._
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jose.util.JSONObjectUtils
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import net.minidev.json.JSONObject

object TestTokenGenerator {

  val claimsRaw: String = """{
                    |  "reports-api": {
                    |    "roles": [
                    |      "employee-reports-view",
                    |      "employee-reports-create",
                    |      "employee-reports-delete",
                    |      "finance-reports-view",
                    |      "finance-reports-create",
                    |      "operations-reports-view"
                    |    ]
                    |  },
                    |  "operations-api": {
                    |    "roles": [
                    |      "operations-schedules-view",
                    |      "operations-schedules-create",
                    |      "operations-schedules-delete",
                    |      "operations-actions-view",
                    |      "operations-actions-create",
                    |      "operations-metrics-view"
                    |    ]
                    |  }
                    |}""".stripMargin

  val claims: JSONObject = JSONObjectUtils.parse(claimsRaw)

  def generateData(withExp: Instant, withNbf: Option[Instant] = None): (SignedJWT, RSAKey, JWKSet) = {

    val claimsSetBuilder = new JWTClaimsSet.Builder()
      .expirationTime(Date.from(withExp))
      .issuer("http://localhost:8080/unit/test")
      .jwtID(UUID.randomUUID().toString)
      .issueTime(Date.from(withExp))
      .claim("resource-access", claims)

    val claimsSet = withNbf.fold(claimsSetBuilder.build()) { nbf =>
      claimsSetBuilder.notBeforeTime(Date.from(nbf)).build()
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

    jwt.sign(signer)

    (jwt, publicKey, jwkSet)
  }
}

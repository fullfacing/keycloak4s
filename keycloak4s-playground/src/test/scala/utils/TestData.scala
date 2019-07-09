package utils

import java.io.File
import java.time.Instant
import java.util.{Date, UUID}

import com.nimbusds.jose._
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jose.util.JSONObjectUtils
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import net.minidev.json.JSONObject

object TestData {

  val rsaJwk: RSAKey = new RSAKeyGenerator(2048)
    .keyID("12345")
    .generate()

  val publicKey: RSAKey = rsaJwk.toPublicJWK

  val obj: JSONObject = JSONObjectUtils.parse(s"""{"keys": [${publicKey.toJSONObject}]}""")

  val file = new File("/keycloak4s/jwks.json")
  val jwks = JWKSet.load(file)

  val jwkSet: JWKSet = JWKSet.parse(obj)

  val signer: JWSSigner = new RSASSASigner(rsaJwk)

  def createToken(withExp: Instant,
                  withIat: Option[Instant] = None,
                  withNbf: Option[Instant] = None,
                  withIss: Option[String] = None,
                  keyIdOverride: Option[String] = None,
                  signerOverride: Option[JWSSigner] = None): SignedJWT = {

    val claimsSetBuilder = new JWTClaimsSet.Builder()
      .expirationTime(Date.from(withExp))
      .jwtID(UUID.randomUUID().toString)

    val addNbf: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withNbf.fold(builder) { nbf =>
      builder.notBeforeTime(Date.from(nbf))
    }

    val addIat: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withIat.fold(builder) { iat =>
      builder.issueTime(Date.from(iat))
    }

    val addIss: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withIss.fold(builder) { iss =>
      builder.issuer(iss)
    }

    val claimsSet = addNbf.andThen(addIat).andThen(addIss)(claimsSetBuilder).build()

    val header: JWSHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
      .keyID(keyIdOverride.getOrElse(rsaJwk.getKeyID))
      .build()

    val jwt: SignedJWT = new SignedJWT(header, claimsSet)

    signerOverride.fold(jwt.sign(signer))(jwt.sign)

    jwt
  }
}
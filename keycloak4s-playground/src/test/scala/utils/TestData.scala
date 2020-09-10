package utils

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

  val obj = JSONObjectUtils.parse(s"""{"keys": [${JSONObject.toJSONString(publicKey.toJSONObject) }]}""")

  val jwkSet: JWKSet = JWKSet.parse(obj)

  val signer: JWSSigner = new RSASSASigner(rsaJwk)

  def createToken(withExp: Option[Instant] = None,
                  withIat: Option[Instant] = None,
                  withNbf: Option[Instant] = None,
                  withIss: Option[String] = None,
                  withResourceAccess: Option[String] = None,
                  keyIdOverride: Option[String] = None,
                  signerOverride: Option[JWSSigner] = None): SignedJWT = {

    val claimsSetBuilder = new JWTClaimsSet.Builder()
      .jwtID(UUID.randomUUID().toString)

    val addExp: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withExp.fold(builder) { exp =>
      builder.expirationTime(Date.from(exp))
    }

    val addNbf: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withNbf.fold(builder) { nbf =>
      builder.notBeforeTime(Date.from(nbf))
    }

    val addIat: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withIat.fold(builder) { iat =>
      builder.issueTime(Date.from(iat))
    }

    val addIss: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withIss.fold(builder) { iss =>
      builder.issuer(iss)
    }

    val addClaims: JWTClaimsSet.Builder => JWTClaimsSet.Builder = builder => withResourceAccess.fold(builder) { permissions =>
      builder.claim("resource_access", JSONObjectUtils.parse(permissions))
    }

    val claimsSet = addExp.andThen(addNbf).andThen(addIat).andThen(addIss).andThen(addClaims)(claimsSetBuilder).build()

    val header: JWSHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
      .keyID(keyIdOverride.getOrElse(rsaJwk.getKeyID))
      .build()

    val jwt: SignedJWT = new SignedJWT(header, claimsSet)

    signerOverride.fold(jwt.sign(signer))(jwt.sign)

    jwt
  }
}
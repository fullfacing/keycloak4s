package com.fullfacing.keycloak4s.auth.core.validation

import java.util.Date

import cats.data.Validated.{invalidNel, valid}
import cats.data.ValidatedNel
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jwt.JWTClaimsSet

trait ClaimValidators {

  /* Validates that a token has an expiration date and has not yet expired. **/
  protected def validateExp(claims: JWTClaimsSet, now: Date): ValidatedNel[KeycloakException, Unit] = {
    val exp   = Option(claims.getExpirationTime)
    val cond  = exp.map(e => now.compareTo(e) < 0)

    lazy val missing    = invalidNel[KeycloakException, Unit](Exceptions.EXP_MISSING)
    lazy val incorrect  = invalidNel[KeycloakException, Unit](Exceptions.EXPIRED)

    cond.fold(missing) {
      case true   => valid(())
      case false  => incorrect
    }
  }

  /* Validates that a token is not being used before its not-before date (if defined). **/
  protected def validateNbf(claims: JWTClaimsSet, now: Date): ValidatedNel[KeycloakException, Unit] = {
    val nbf   = Option(claims.getNotBeforeTime)
    val cond  = nbf.fold(true)(n => n == new Date(0) || now.compareTo(n) > 0)

    if (cond) valid(()) else invalidNel(Exceptions.NOT_YET_VALID)
  }

  /* Validates that a token's 'issued at' date exists and is not in the future. **/
  protected def validateIat(claims: JWTClaimsSet, now: Date): ValidatedNel[KeycloakException, Unit] = {
    val iat   = Option(claims.getIssueTime)
    val cond  = iat.map(now.compareTo(_) > 0)

    lazy val missing    = invalidNel[KeycloakException, Unit](Exceptions.IAT_MISSING)
    lazy val incorrect  = invalidNel[KeycloakException, Unit](Exceptions.IAT_INCORRECT)

    cond.fold(missing) {
      case true   => valid(())
      case false  => incorrect
    }
  }

  /* Validates that a token's issuer is defined and matches the web address as specified in the KeycloakConfig. **/
  protected def validateIss(claims: JWTClaimsSet, uri: String, proxy: Option[String] = None): ValidatedNel[KeycloakException, Unit] = {
    val issuer = Option(claims.getIssuer)
    val cond   = issuer.map(iss => iss == uri || proxy.contains(iss))

    lazy val missing    = invalidNel[KeycloakException, Unit](Exceptions.ISS_MISSING)
    lazy val incorrect  = invalidNel[KeycloakException, Unit](Exceptions.ISS_INCORRECT)

    cond.fold(missing) {
      case true   => valid(())
      case false  => incorrect
    }
  }
}

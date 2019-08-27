package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.authorization.Authorization
import com.fullfacing.keycloak4s.auth.akka.http.authorization.Authorization._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.core.models.AuthPayload
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.auth.core.validation.TokenValidator

trait SecurityMagnet {
  def apply(): Directive1[AuthPayload]
}

object SecurityMagnet {

  /* Authorizes a request with a correlation ID passed through. **/
  implicit def run(parameters: (Authorization, UUID))(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    val (securityConfig, cId) = parameters
    authorize(securityConfig, cId)
  }

  /* Authorizes a request and generates a new correlation ID. **/
  implicit def run(securityConfig: Authorization)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    authorize(securityConfig, UUID.randomUUID())
  }

  private def authorize(securityConfig: Authorization, correlationId: => UUID)(implicit tokenValidator: TokenValidator): Directive1[AuthPayload] = {
    validateToken(correlationId).tflatMap { case (cId, authPayload) =>
      if (securityConfig.policyDisabled()) {
        provide(authPayload)
      } else {
        authorizeResourceServerAccess(authPayload, securityConfig.service)(cId).flatMap { case (path, method, userRoles) =>
          val isAuthorized = securityConfig.authorizeRequest(path, method, userRoles)(cId)

          if (isAuthorized) {
            Logging.requestAuthorized(cId)
            provide(authPayload)
          } else {
            authorizationFailed()
          }
        }
      }
    }
  }
}
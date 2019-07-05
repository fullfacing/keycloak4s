package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.pass
import com.fullfacing.keycloak4s.auth.akka.http.Logging
import com.fullfacing.keycloak4s.auth.akka.http.authorization.Authorization
import com.fullfacing.keycloak4s.auth.akka.http.authorization.Authorization._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.validation.TokenValidator

trait SecurityMagnet {
  def apply(): Directive0
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

  private def authorize(securityConfig: Authorization, correlationId: => UUID)(implicit tokenValidator: TokenValidator): Directive0 = {
    validateToken(correlationId).tflatMap { case (cId, authPayload) =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authorizeResourceServerAccess(authPayload, securityConfig.service)(cId).flatMap { case (path, method, userRoles) =>
          val isAuthorized = securityConfig.authorizeRequest(path, method, userRoles)(cId)

          if (isAuthorized) {
            Logging.requestAuthorized(cId)
            pass
          } else {
            authorizationFailed()
          }
        }
      }
    }
  }
}
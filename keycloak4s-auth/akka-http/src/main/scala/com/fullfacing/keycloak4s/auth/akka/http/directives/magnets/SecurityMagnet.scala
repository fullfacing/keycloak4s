package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.directives.{AuthDirectives, ValidationDirectives}
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization.AuthRequest
import com.fullfacing.keycloak4s.auth.core.models.AuthPayload
import com.fullfacing.keycloak4s.auth.core.validation.TokenValidator
import cats.effect.unsafe.IORuntime

trait SecurityMagnet {
  def apply(): Directive1[AuthPayload]
}

object SecurityMagnet extends AuthDirectives with ValidationDirectives {

  /* Authorizes a request with a correlation ID passed through. **/
  implicit def run(parameters: (PathAuthorization, UUID))(implicit tokenValidator: TokenValidator, ioRuntime: IORuntime): SecurityMagnet = { () =>
    val (securityConfig, cId) = parameters
    authorize(securityConfig, cId)
  }

  /* Authorizes a request and generates a new correlation ID. **/
  implicit def run(securityConfig: PathAuthorization)(implicit tokenValidator: TokenValidator, ioRuntime: IORuntime): SecurityMagnet = { () =>
    authorize(securityConfig, UUID.randomUUID())
  }

  private def authorize(securityConfig: PathAuthorization, correlationId: => UUID)(implicit tokenValidator: TokenValidator, ioRuntime: IORuntime): Directive1[AuthPayload] =
    validateToken(correlationId).tflatMap { case (cId, authPayload) =>
      if (securityConfig.policyDisabled()) {
        provide(authPayload)
      } else {
        authorizeResourceServerAccess(authPayload, securityConfig.service, securityConfig.pathMatchingMode)(cId).flatMap { case (path, method, userRoles) =>
          val isAuthorized = securityConfig.authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))(cId)

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
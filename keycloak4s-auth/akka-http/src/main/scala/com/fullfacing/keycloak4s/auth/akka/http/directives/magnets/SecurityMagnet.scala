package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractMethod, extractUnmatchedPath, pass}
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.Authorisation
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.Authorisation._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.validation.TokenValidator

trait SecurityMagnet {
  def apply(): Directive0
}

object SecurityMagnet {

  implicit def authorise(securityConfig: Authorisation)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    implicit val correlationId: UUID = UUID.randomUUID()

    validateToken().flatMap { authPayload =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authoriseResourceServerAccess(authPayload, securityConfig.service).flatMap { userRoles =>
          extractUnmatchedPath.flatMap { path =>
            extractMethod.flatMap { method =>
              val isAuthorised = securityConfig.authoriseRequest(path, method, userRoles)
              if (isAuthorised) pass else authorisationFailed()
            }
          }
        }
      }
    }
  }
}
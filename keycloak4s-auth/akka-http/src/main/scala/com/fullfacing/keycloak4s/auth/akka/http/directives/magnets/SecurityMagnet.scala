package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractMethod, extractUnmatchedPath, pass}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.models.SecurityConfig
import com.fullfacing.keycloak4s.auth.akka.http.services.Authorisation._
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator

trait SecurityMagnet {
  def apply(): Directive0
}

object SecurityMagnet {

  implicit def authorise(securityConfig: SecurityConfig)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    validateToken().flatMap { p =>
      authoriseResourceServerAccess(p, securityConfig.service).flatMap { userRoles =>
        extractUnmatchedPath.flatMap { path =>
          extractMethod.flatMap { method =>
            val isAuthorised = authoriseRequest(path, method, securityConfig, userRoles)
            if (isAuthorised) pass else authorisationFailed()
          }
        }
      }
    }
  }
}
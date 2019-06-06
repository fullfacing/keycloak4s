package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractMethod, extractUnmatchedPath, pass}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.models.{NodeConfiguration, PathConfiguration}
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.PathAuthorisation
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.NodeAuthorisation
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.Utilities._
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator

trait SecurityMagnet {
  def apply(): Directive0
}

object SecurityMagnet {

  implicit def authorise(securityConfig: NodeConfiguration)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    validateToken().flatMap { p =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authoriseResourceServerAccess(p, securityConfig.service).flatMap { userRoles =>
          extractUnmatchedPath.flatMap { path =>
            extractMethod.flatMap { method =>
              val isAuthorised = NodeAuthorisation.authoriseRequest(path, method, securityConfig, userRoles)
              if (isAuthorised) pass else authorisationFailed()
            }
          }
        }
      }
    }
  }

  implicit def authoriseToo(securityConfig: PathConfiguration)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    validateToken().flatMap { p =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authoriseResourceServerAccess(p, securityConfig.service).flatMap { userRoles =>
          extractUnmatchedPath.flatMap { path =>
            extractMethod.flatMap { method =>
              val isAuthorised = PathAuthorisation.authoriseRequest(path, method, securityConfig, userRoles)
              if (isAuthorised) pass else authorisationFailed()
            }
          }
        }
      }
    }
  }
}
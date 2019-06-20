package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

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
    validateToken().flatMap { p =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authoriseResourceServerAccess(p, securityConfig.service).flatMap { userRoles =>
          extractUnmatchedPath.flatMap { path =>
            extractMethod.flatMap { method =>
              val t1 = System.nanoTime()
              val isAuthorised = securityConfig.authoriseRequest(path, method, userRoles)
              val t2 = System.nanoTime()
              println((t2 - t1)/1000000.00)
              if (isAuthorised) pass else authorisationFailed()
            }
          }
        }
      }
    }
  }
}
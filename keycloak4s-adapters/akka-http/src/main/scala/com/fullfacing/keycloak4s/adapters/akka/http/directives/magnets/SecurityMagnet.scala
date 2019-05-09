package com.fullfacing.keycloak4s.adapters.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive0
import com.fullfacing.keycloak4s.adapters.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.adapters.akka.http.models.SecurityConfig
import com.fullfacing.keycloak4s.adapters.akka.http.services.Authorisation._
import com.fullfacing.keycloak4s.adapters.akka.http.services.TokenValidator

trait SecurityMagnet {
  def apply(): Directive0
}

object SecurityMagnet {

  implicit def authorise(resourceServer: SecurityConfig)(implicit tokenValidator: TokenValidator): SecurityMagnet = () => {
    validateToken().flatMap { p =>
      authoriseResourceServerAccess(p, resourceServer.service).flatMap { sp =>
        authoriseRequest(resourceServer.nodes, sp)
      }
    }
  }
}


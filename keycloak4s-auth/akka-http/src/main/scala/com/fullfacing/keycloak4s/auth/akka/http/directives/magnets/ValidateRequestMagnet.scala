package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive1
import com.fullfacing.keycloak4s.auth.akka.http.models.Permissions
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.services.{Authorisation, TokenValidator}

trait ValidateRequestMagnet {
  def apply(): Directive1[Permissions]
}

object ValidateRequestMagnet {

  implicit def validateRequest(resourceServer: String)(implicit tokenValidator: TokenValidator): ValidateRequestMagnet = () => {
    validateToken().flatMap { p =>
      Authorisation.authoriseResourceServerAccess(p, resourceServer)
    }
  }
}
package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.models.{AuthPayload, ResourceRoles}
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator

trait ValidateRequestMagnet {
  def apply(): Directive1[AuthPayload]
}

object ValidateRequestMagnet {

  implicit def validateRequest(resourceServer: String)(implicit tokenValidator: TokenValidator): ValidateRequestMagnet = () => {
    validateToken().flatMap { p =>
      authoriseResourceServerAccess(p, resourceServer)
    }
  }

  /**
   * Top level authorisation.
   * Authorises user's access to the service and returns only permissions specific to the service.
   *
   * @param permissions     All permissions in user's token.
   * @param resourceServer  Name of the resource-server.
   * @return                Permissions specific to given resource server.
   */
  def authoriseResourceServerAccess(permissions: AuthPayload, resourceServer: String): Directive1[AuthPayload] = {
    val f = { r: ResourceRoles =>
      val rr = r.roles.map(_.split("-")).groupBy(_.headOption).collect { case (Some(k), v) =>
        k -> ResourceRoles(v.flatMap(_.lastOption))
      }
      permissions.copy(resourceRoles = rr)
    }

    checkPermissions(resourceServer, permissions, r => provide(f(r)))
  }
}
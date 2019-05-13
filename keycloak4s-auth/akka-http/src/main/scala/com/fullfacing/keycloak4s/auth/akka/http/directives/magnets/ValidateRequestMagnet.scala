package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.directives.ValidationDirective
import com.fullfacing.keycloak4s.auth.akka.http.models.{Permissions, ResourceRoles}
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator

trait ValidateRequestMagnet {
  def apply(): Directive1[Permissions]
}

object ValidateRequestMagnet extends ValidationDirective {

  implicit def validateRequest(resourceServer: String)(implicit tokenValidator: TokenValidator): ValidateRequestMagnet = () => {
    implicit val correlationId: UUID = UUID.randomUUID()
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
  private def authoriseResourceServerAccess(permissions: Permissions, resourceServer: String): Directive1[Permissions] = {
    val f = { r: ResourceRoles =>
      val rr = r.roles.map(_.split("-")).groupBy(_.headOption).collect { case (Some(k), v) =>
        k -> ResourceRoles(v.flatMap(_.lastOption))
      }
      permissions.copy(resources = rr)
    }

    checkPermissions(resourceServer, permissions, r => provide(f(r)))
  }
}
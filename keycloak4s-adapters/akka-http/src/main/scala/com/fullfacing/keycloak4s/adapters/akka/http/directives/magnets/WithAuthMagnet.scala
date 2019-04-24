package com.fullfacing.keycloak4s.adapters.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive0
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.adapters.akka.http.models.ResourceAccess

trait WithAuthMagnet {
  type Result = Directive0

  def apply(): Result
}

object WithAuthMagnet {

  implicit def withAuth(resource: String)(implicit permissions: ResourceAccess): WithAuthMagnet = () => {
    checkPermissions(resource, permissions, authoriseMethod)
  }
}

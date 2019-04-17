package com.fullfacing.keycloak4s.adapters.akka.http.directives.magents

import akka.http.scaladsl.server.Directive0
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.adapters.akka.http.models.Permissions

trait WithAuthMagnet {
  type Result = Directive0

  def apply(): Result
}

object WithAuthMagnet {

  implicit def withAuth(resource: String)(implicit permissions: Permissions): WithAuthMagnet = () => {
    checkPermissions(resource, permissions, authoriseMethod)
  }
}

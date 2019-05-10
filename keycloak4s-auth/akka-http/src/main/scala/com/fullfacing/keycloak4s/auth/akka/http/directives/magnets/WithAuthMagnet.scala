package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive0
import com.fullfacing.keycloak4s.auth.akka.http.models.Permissions
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives.authoriseMethod

trait WithAuthMagnet {
  type Result = Directive0

  def apply(): Result
}

object WithAuthMagnet {

  implicit def withAuth(resource: String)(implicit permissions: Permissions): WithAuthMagnet = () => {
    checkPermissions(resource, permissions, authoriseMethod)
  }
}

package com.fullfacing.keycloak4s.adapters.akka.http.directives.magents

import akka.http.scaladsl.server.Directives.provide
import akka.http.scaladsl.server.{Directive0, Directive1}
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.adapters.akka.http.models.{Permissions, ResourceMethods}

trait AuthoriseResourceMagnet {
  type Result = Directive1[ResourceMethods]

  def apply(): Result
}

object AuthoriseResourceMagnet {
  implicit def authoriseResource(resource: String)(implicit permissions: Permissions): AuthoriseResourceMagnet = () => {
    checkPermissions(resource, permissions, provide)
  }
}

trait WithAuthMagnet {
  type Result = Directive0

  def apply(): Result
}

object WithAuthMagnet {
  implicit def withAuth(resource: String)(implicit permissions: Permissions): WithAuthMagnet = () => {
    checkPermissions(resource, permissions, authoriseMethod)
  }
}
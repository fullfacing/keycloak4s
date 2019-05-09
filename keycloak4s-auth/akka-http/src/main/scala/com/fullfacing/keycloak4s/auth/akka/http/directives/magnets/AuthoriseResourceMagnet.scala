package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.models.{Permissions, ResourceRoles}

trait AuthoriseResourceMagnet {
  type Result = Directive1[ResourceRoles]

  def apply(): Result
}

object AuthoriseResourceMagnet {

  implicit def authoriseResource(resource: String)(implicit permissions: Permissions): AuthoriseResourceMagnet = () => {
    checkPermissions(resource, permissions, provide)
  }
}
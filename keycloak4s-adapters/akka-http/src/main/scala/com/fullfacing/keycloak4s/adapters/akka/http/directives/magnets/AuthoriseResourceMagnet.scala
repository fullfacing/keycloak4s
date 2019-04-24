package com.fullfacing.keycloak4s.adapters.akka.http.directives.magnets

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.adapters.akka.http.models.{ResourceAccess, ResourceRoles}

trait AuthoriseResourceMagnet {
  type Result = Directive1[ResourceRoles]

  def apply(): Result
}

object AuthoriseResourceMagnet {

  implicit def authoriseResource(resource: String)(implicit permissions: ResourceAccess): AuthoriseResourceMagnet = () => {
    checkPermissions(resource, permissions, provide)
  }
}
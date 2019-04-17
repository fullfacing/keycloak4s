package com.fullfacing.keycloak4s.adapters.akka.http.directives.magents

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
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
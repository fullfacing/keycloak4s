package com.fullfacing.keycloak4s.adapters.akka.http.directives.magents

import java.util.UUID

import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.{Directive, Directive1, PathMatcher}
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.adapters.akka.http.models.{Permissions, ResourceMethods}

trait PathWithAuthMagnet[A] {
  type Result = Directive[A]

  def apply(): Result
}

object PathWithAuthMagnet {

  implicit def thePath(resource: String)(implicit permissions: Permissions): PathWithAuthMagnet[Tuple1[ResourceMethods]] =
    new PathWithAuthMagnet[Tuple1[ResourceMethods]] {
      override type Result = Directive1[ResourceMethods]

      override def apply(): Result = {
        checkPermissions(resource, permissions, r => path(resource).tmap(_ => r))
      }
    }

  implicit def thePath2(params: (String, PathMatcher[Tuple1[UUID]]))(implicit permissions: Permissions): PathWithAuthMagnet[(ResourceMethods, UUID)] =
    new PathWithAuthMagnet[(ResourceMethods, UUID)] {
      override type Result = Directive[(ResourceMethods, UUID)]

      override def apply(): Result = {
        val (resource, pm) = params
        checkPermissions(resource, permissions, v => path(resource / pm).tmap { case Tuple1(a) => (v, a) })
      }
    }

  implicit def thePath3(params: (PathMatcher[Tuple1[UUID]], String))(implicit permissions: Permissions): PathWithAuthMagnet[(ResourceMethods, UUID)] =
    new PathWithAuthMagnet[(ResourceMethods, UUID)] {
      override type Result = Directive[(ResourceMethods, UUID)]

      override def apply(): Result = {
        val (pm, resource) = params
        checkPermissions(resource, permissions, v => path(pm / resource).tmap { case Tuple1(a) => (v, a) })
      }
    }
}

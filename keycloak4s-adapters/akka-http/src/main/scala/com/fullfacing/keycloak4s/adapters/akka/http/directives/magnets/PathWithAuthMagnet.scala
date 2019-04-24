package com.fullfacing.keycloak4s.adapters.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.{Directive, Directive1, PathMatcher}
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.adapters.akka.http.models.{ResourceAccess, ResourceRoles}

trait PathWithAuthMagnet[A] {
  type Result = Directive[A]

  def apply(): Result
}

object PathWithAuthMagnet {

  implicit def simplePath(resource: String)(implicit permissions: ResourceAccess): PathWithAuthMagnet[Tuple1[ResourceRoles]] =
    new PathWithAuthMagnet[Tuple1[ResourceRoles]] {
      override type Result = Directive1[ResourceRoles]

      override def apply(): Result = {
        checkPermissions(resource, permissions, r => path(resource).tmap(_ => r))
      }
    }

  implicit def tuplePath1(params: (String, PathMatcher[Tuple1[UUID]]))(implicit permissions: ResourceAccess): PathWithAuthMagnet[(ResourceRoles, UUID)] =
    new PathWithAuthMagnet[(ResourceRoles, UUID)] {
      override type Result = Directive[(ResourceRoles, UUID)]

      override def apply(): Result = {
        val (resource, pm) = params
        checkPermissions(resource, permissions, v => path(resource / pm).tmap { case Tuple1(a) => (v, a) })
      }
    }

  implicit def tuplePath2(params: (PathMatcher[Tuple1[UUID]], String))(implicit permissions: ResourceAccess): PathWithAuthMagnet[(ResourceRoles, UUID)] =
    new PathWithAuthMagnet[(ResourceRoles, UUID)] {
      override type Result = Directive[(ResourceRoles, UUID)]

      override def apply(): Result = {
        val (pm, resource) = params
        checkPermissions(resource, permissions, v => path(pm / resource).tmap { case Tuple1(a) => (v, a) })
      }
    }
}

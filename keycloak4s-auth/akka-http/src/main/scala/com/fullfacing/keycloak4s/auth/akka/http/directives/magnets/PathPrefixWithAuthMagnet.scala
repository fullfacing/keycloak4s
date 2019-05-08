package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.{Directive, Directive1, PathMatcher}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.models.{Permissions, ResourceRoles}

trait PathPrefixWithAuthMagnet[A] {
  type Result = Directive[A]

  def apply(): Result
}

object PathPrefixWithAuthMagnet {

  implicit def simplePathPrefix(resource: String)(implicit permissions: Permissions): PathPrefixWithAuthMagnet[Tuple1[ResourceRoles]] =
    new PathPrefixWithAuthMagnet[Tuple1[ResourceRoles]] {
      override type Result = Directive1[ResourceRoles]

      override def apply(): Result = {
        checkPermissions(resource, permissions, r => pathPrefix(resource).tmap(_ => r))
      }
    }

  implicit def tuplePathPrefix1(params: (String, PathMatcher[Tuple1[UUID]]))(implicit permissions: Permissions): PathPrefixWithAuthMagnet[(ResourceRoles, UUID)] =
    new PathPrefixWithAuthMagnet[(ResourceRoles, UUID)] {
      override type Result = Directive[(ResourceRoles, UUID)]

      override def apply(): Result = {
        val (resource, pm) = params
        checkPermissions(resource, permissions, v => pathPrefix(resource / pm).tmap { case Tuple1(a) => (v, a) })
      }
    }

  implicit def tuplePathPrefix2(params: (PathMatcher[Tuple1[UUID]], String))(implicit permissions: Permissions): PathPrefixWithAuthMagnet[(ResourceRoles, UUID)] =
    new PathPrefixWithAuthMagnet[(ResourceRoles, UUID)] {
      override type Result = Directive[(ResourceRoles, UUID)]

      override def apply(): Result = {
        val (pm, resource) = params
        checkPermissions(resource, permissions, v => pathPrefix(pm / resource).tmap { case Tuple1(a) => (v, a) })
      }
    }
}
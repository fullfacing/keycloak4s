package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.{Directive, Directive1, PathMatcher}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.models.{AuthPayload, AuthRoles}

trait PathPrefixWithAuthMagnet[A] {
  type Result = Directive[A]

  def apply(): Result
}

object PathPrefixWithAuthMagnet {

  implicit def simplePathPrefix(resource: String)(implicit permissions: AuthPayload): PathPrefixWithAuthMagnet[Tuple1[AuthRoles]] =
    new PathPrefixWithAuthMagnet[Tuple1[AuthRoles]] {
      override type Result = Directive1[AuthRoles]

      override def apply(): Result = {
        checkPermissions(resource, permissions, r => pathPrefix(resource).tmap(_ => r))
      }
    }

  implicit def tuplePathPrefix1(params: (String, PathMatcher[Tuple1[UUID]]))(implicit permissions: AuthPayload): PathPrefixWithAuthMagnet[(AuthRoles, UUID)] =
    new PathPrefixWithAuthMagnet[(AuthRoles, UUID)] {
      override type Result = Directive[(AuthRoles, UUID)]

      override def apply(): Result = {
        val (resource, pm) = params
        checkPermissions(resource, permissions, v => pathPrefix(resource / pm).tmap { case Tuple1(a) => (v, a) })
      }
    }

  implicit def tuplePathPrefix2(params: (PathMatcher[Tuple1[UUID]], String))(implicit permissions: AuthPayload): PathPrefixWithAuthMagnet[(AuthRoles, UUID)] =
    new PathPrefixWithAuthMagnet[(AuthRoles, UUID)] {
      override type Result = Directive[(AuthRoles, UUID)]

      override def apply(): Result = {
        val (pm, resource) = params
        checkPermissions(resource, permissions, v => pathPrefix(pm / resource).tmap { case Tuple1(a) => (v, a) })
      }
    }
}
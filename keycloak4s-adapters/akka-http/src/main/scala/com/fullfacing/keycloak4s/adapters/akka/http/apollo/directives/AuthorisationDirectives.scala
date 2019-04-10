package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.server.directives.MethodDirectives
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.Permissions

trait AuthorisationDirectives extends MethodDirectives {

  //TODO Determine if OPTIONS method will require authentication.
  private def scopeMap(method: HttpMethod): Option[String] = method match {
    case HttpMethods.GET | HttpMethods.HEAD                     => Some("view")
    case HttpMethods.POST | HttpMethods.PUT | HttpMethods.PATCH => Some("create")
    case HttpMethods.DELETE                                     => Some("delete")
    case _                                                      => None
  }

  private def authorizeMethod(permissions: Permissions): Directive0 = {
    extractMethod.flatMap { method =>
      authorize {
        scopeMap(method).exists(required => permissions.scopes.contains(required))
      }
    }
  }

  //TODO Find way to implicitly call Permissions without breaking the subsequent closure.
  def getA(p: Permissions): Directive0    = get.tflatMap(_ => authorizeMethod(p))
  def putA(p: Permissions): Directive0    = put.tflatMap(_ => authorizeMethod(p))
  def headA(p: Permissions): Directive0   = head.tflatMap(_ => authorizeMethod(p))
  def postA(p: Permissions): Directive0   = post.tflatMap(_ => authorizeMethod(p))
  def patchA(p: Permissions): Directive0  = patch.tflatMap(_ => authorizeMethod(p))
  def deleteA(p: Permissions): Directive0 = delete.tflatMap(_ => authorizeMethod(p))


  def withAuth(role: String, permissions: Permissions): Directive0 = {
    authorize {
      permissions.roles.contains(role)
    }
  }
}
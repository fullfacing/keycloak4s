package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.server.directives.MethodDirectives
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext

trait AuthorisationDirectives extends MethodDirectives {

  //TODO Determine if OPTIONS method will require authentication.
  private def scopeMap(method: HttpMethod): String = method match {
    case HttpMethods.GET | HttpMethods.HEAD => "view"
    case HttpMethods.POST | HttpMethods.PUT | HttpMethods.PATCH => "create"
    case HttpMethods.DELETE => "delete"
  }

  private def authorizeMethod(ctx: RequestContext): Directive0 = {
    extractMethod.flatMap { method =>
      authorize {
        ctx.permissions.scopes.contains(scopeMap(method))
      }
    }
  }

  //TODO Find way to implicitly call RequestContext without breaking the subsequent closure.
  def deleteA(ctx: RequestContext): Directive0 = delete.tflatMap(_ => authorizeMethod(ctx))
  def getA(ctx: RequestContext): Directive0 = get.tflatMap(_ => authorizeMethod(ctx))
  def headA(ctx: RequestContext): Directive0 = head.tflatMap(_ => authorizeMethod(ctx))
  def patchA(ctx: RequestContext): Directive0 = patch.tflatMap(_ => authorizeMethod(ctx))
  def postA(ctx: RequestContext): Directive0 = post.tflatMap(_ => authorizeMethod(ctx))
  def putA(ctx: RequestContext): Directive0 = put.tflatMap(_ => authorizeMethod(ctx))

  def withAuth(role: String, ctx: RequestContext): Directive0 = {
    authorize {
      ctx.permissions.roles.contains(role)
    }
  }
}
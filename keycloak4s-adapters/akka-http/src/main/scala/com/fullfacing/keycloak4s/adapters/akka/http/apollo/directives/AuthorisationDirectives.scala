package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.fullfacing.apollo.http.directives.TaskDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext

trait AuthorisationDirectives extends TaskDirectives {

  private def scopeMap(method: HttpMethod): String = method match {
    case HttpMethods.GET || HttpMethods.HEAD => "view"
    case HttpMethods.POST || HttpMethods.PUT || HttpMethods.PATCH => "create"
    case HttpMethods.DELETE => "delete"
  }

  def authorise(resources: String*)(implicit ctx: RequestContext): Directive0 = {
    extractMethod.flatMap { method =>
      val scopeAllowed = ctx.permissions.scopes.contains(scopeMap(method))
      val roleAllowed = resources.forall(ctx.permissions.roles.contains)
      authorize(scopeAllowed && roleAllowed)
    }
  }
}
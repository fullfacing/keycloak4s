package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.fullfacing.apollo.http.directives.TaskDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext

trait AuthorisationDirectives extends TaskDirectives {

  /** WIP - authorisation directive */
  def authorise(ctx: RequestContext, required: List[String]): Directive0 = {


    authorize {
      val permissions = ctx.permissions.roles ++ ctx.permissions.scopes
      required.forall(permissions.contains)
    }
  }

}

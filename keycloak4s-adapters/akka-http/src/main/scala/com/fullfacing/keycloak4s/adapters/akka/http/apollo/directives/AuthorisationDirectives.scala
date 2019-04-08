package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.fullfacing.apollo.http.directives.TaskDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext

trait AuthorisationDirectives extends TaskDirectives {

  /** WIP - authorisation directive */
  def authorise(ctx: RequestContext, found: List[String]): Directive0 = {
    authorize(true)
  }

}

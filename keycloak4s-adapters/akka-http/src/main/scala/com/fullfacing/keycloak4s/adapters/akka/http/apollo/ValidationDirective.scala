package com.fullfacing.keycloak4s.adapters.akka.http.apollo

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import com.fullfacing.apollo.http.directives.TaskDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.TokenValidator
import com.nimbusds.jose.Payload

trait ValidationDirective extends TaskDirectives {

  def validateToken(context: RequestContext): Directive1[RequestContext] = {
    context.token match {
      case Some(token) => callValidation(token, context)
      case None        => reject(AuthorizationFailedRejection)
    }
  }

  private def callValidation(token: String, context: RequestContext): Directive1[RequestContext] = {
    TokenValidator.validate(token)(List.empty) match {
      case Right(r) => provide(updateRequestContext(r, context))
      case Left(_)  => reject(AuthorizationFailedRejection)
    }
  }

  private def updateRequestContext(result: Payload, context: RequestContext): RequestContext = {
    context.copy(
      permissions = Some(result)
    )
  }
}

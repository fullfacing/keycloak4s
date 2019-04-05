package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, provide}
import com.fullfacing.apollo.http.directives.TaskDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext
import com.fullfacing.keycloak4s.adapters.akka.http.{Errors, TokenValidator}
import com.nimbusds.jose.Payload

trait ValidationDirective extends TaskDirectives {

  def validateToken(context: RequestContext)(implicit tv: TokenValidator): Directive1[RequestContext] = {
    context.token match {
      case Some(token) => callValidation(token, context)
      case None        => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, "No token provided"))
    }
  }

  private def callValidation(token: String, context: RequestContext)(implicit validator: TokenValidator): Directive1[RequestContext] = {
    validator.validate(token) match {
      case Right(r) => provide(updateRequestContext(r, context))
      case Left(t)  => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, t.getMessage))
    }
  }

  private def updateRequestContext(result: Payload, context: RequestContext): RequestContext = {
    context.copy(
      permissions = Some(result)
    )
  }
}

package com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.RequestContext
import com.fullfacing.keycloak4s.adapters.akka.http.{Errors, TokenValidator}
import com.nimbusds.jose.Payload
import monix.execution.Scheduler

import scala.util.{Failure, Success}

trait ValidationDirective {

  /**
   * Extracts the token from the RequestContext and has it validated.
   *
   * @param context the initial request context containing the access token
   * @return        directive with the updated RequestContext containing the verified user's permissions
   */
  def validateToken(context: RequestContext)(implicit tv: TokenValidator, scheduler: Scheduler): Directive1[RequestContext] = {
    context.token match {
      case Some(token) => callValidation(token, context)
      case None        => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, "No token provided"))
    }
  }

  /** Runs the validation function. */
  private def callValidation(token: String, context: RequestContext)(implicit validator: TokenValidator, scheduler: Scheduler): Directive1[RequestContext] = {
    onComplete(validator.validate(token).runToFuture).flatMap {
      case Success(r) => handleValidationResponse(r, context)
      case Failure(e) => complete(Errors.errorResponse(StatusCodes.InternalServerError.intValue, "An unexpected error occurred", Some(e.getMessage)))
    }
  }

  /** Handles the success/failure of the token validation. */
  private def handleValidationResponse(response: Either[Throwable, Payload], context: RequestContext): Directive1[RequestContext] = response match {
    case Right(r) => provide(updateRequestContext(r, context))
    case Left(t)  => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, t.getMessage))
  }

  /** Injects the user permissions from the unpacked token into the request context. */
  private def updateRequestContext(result: Payload, context: RequestContext): RequestContext = {
    context.copy(
      permissions = Some(result)
    )
  }
}

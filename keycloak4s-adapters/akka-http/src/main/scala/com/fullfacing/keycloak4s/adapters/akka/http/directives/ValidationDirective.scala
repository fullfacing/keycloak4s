package com.fullfacing.keycloak4s.adapters.akka.http.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.fullfacing.keycloak4s.adapters.akka.http.Errors
import com.fullfacing.keycloak4s.adapters.akka.http.models.Permissions
import com.fullfacing.keycloak4s.adapters.akka.http.services.TokenValidator
import com.nimbusds.jose.Payload
import org.json4s.Formats
import org.json4s.jackson.JsonMethods.parse

import scala.util.{Failure, Success, Try}

trait ValidationDirective {

  implicit val formats: Formats = org.json4s.DefaultFormats

  /**
   * Extracts the token from the RequestContext and has it validated.
   *
   * @return        directive with the updated RequestContext containing the verified user's permissions
   */
  def validateToken(implicit tv: TokenValidator): Directive1[Permissions] = {
    extractCredentials.flatMap {
      case Some(token) => callValidation(token.token())
      case None        => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, "No token provided"))
    }
  }

  /** Runs the validation function. */
  private def callValidation(token: String)(implicit validator: TokenValidator): Directive1[Permissions] = {
    onComplete(validator.validate(token).unsafeToFuture()).flatMap {
      case Success(r) => handleValidationResponse(r)
      case Failure(e) => complete(Errors.errorResponse(StatusCodes.InternalServerError.intValue, "An unexpected error occurred", Some(e.getMessage)))
    }
  }

  /** Handles the success/failure of the token validation. */
  private def handleValidationResponse(response: Either[Throwable, Payload]): Directive1[Permissions] = response match {
    case Right(r) => provide(updateRequestContext(r))
    case Left(t)  => complete(Errors.errorResponse(StatusCodes.Unauthorized.intValue, t.getMessage))
  }

  /** Injects the user permissions from the unpacked token into the request context. */
  private def updateRequestContext(result: Payload): Permissions = {
    val json = parse(result.toString)

    val scopes = Try {
      (json \\ "scope").extract[String].split(" ").toList
    }.getOrElse(Nil)

    val roles = Try {
      (json \\ "realm_access" \\ "roles").extract[List[String]]
    }.getOrElse(Nil)

    Permissions(scopes = scopes, roles = roles)
  }
}

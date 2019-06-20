package com.fullfacing.keycloak4s.auth.akka.http.directives

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, extractCredentials, onComplete, optionalHeaderValueByName, provide}
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator
import com.fullfacing.keycloak4s.core.models.KeycloakException

import scala.util.{Failure, Success}

trait ValidationDirective {

  /**
   * Token Validation directive to secure all inner directives.
   * Extracts the token and has it validated by the implicit instance of the TokenValidator.
   * The resource_access field is extracted from the token and provided for authorisation on
   * on inner directives.
   *
   * @return  Directive with verified user's permissions
   */
  def validateToken()(implicit tv: TokenValidator): Directive1[AuthPayload] = {
    optionalHeaderValueByName("Id-Token").flatMap { idToken =>
      extractCredentials.flatMap {
        case Some(token)  => callValidation(token.token(), idToken)
        case None         => complete(HttpResponse(StatusCodes.Unauthorized, entity = "No token provided"))
      }
    }
  }

  /** Runs the validation function. */
  private def callValidation(token: String, idToken: Option[String])(implicit validator: TokenValidator): Directive1[AuthPayload] = {
    val task = idToken.fold(validator.process(token))(validator.parProcess(token, _))

    onComplete(task.unsafeToFuture()).flatMap {
      case Success(r) => handleValidationResponse(r)
      case Failure(_) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An unexpected error occurred"))
    }
  }

  /** Handles the success/failure of the token validation. */
  private def handleValidationResponse(response: Either[KeycloakException, AuthPayload]): Directive1[AuthPayload] = response match {
    case Right(r) => provide(r)
    case Left(t)  => complete(HttpResponse(status = t.code, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, t.getMessage)))
  }
}

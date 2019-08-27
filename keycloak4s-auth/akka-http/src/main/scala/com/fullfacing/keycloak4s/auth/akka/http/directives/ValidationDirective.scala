package com.fullfacing.keycloak4s.auth.akka.http.directives

import java.util.UUID

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directive, Directive1}
import akka.http.scaladsl.server.Directives.{complete, extractCredentials, onComplete, optionalHeaderValueByName, provide}
import com.fullfacing.keycloak4s.auth.core.models.AuthPayload
import com.fullfacing.keycloak4s.auth.core.validation.TokenValidator
import com.fullfacing.keycloak4s.core.models.KeycloakException

import scala.util.{Failure, Success}

trait ValidationDirective {

  type AuthPayloadWithId = (UUID, AuthPayload)

  /**
   * Token Validation directive to secure all inner directives.
   * Extracts the token and has it validated by the implicit instance of the TokenValidator.
   * The resource_access field is extracted from the token and provided for authorization on
   * on inner directives.
   *
   * @return  Directive with verified user's permissions
   */
  def validateToken(cId: => UUID)(implicit tv: TokenValidator): Directive[AuthPayloadWithId] = {
    optionalHeaderValueByName("Id-Token").flatMap { idToken =>
      extractCredentials.flatMap {
        case Some(token)  => callValidation(token.token(), idToken)(tv, cId)
        case None         => complete(HttpResponse(StatusCodes.Unauthorized, entity = "No token provided"))
      }
    }
  }

  /** Runs the validation function. */
  private def callValidation(token: String, idToken: Option[String])(implicit validator: TokenValidator, cId: UUID): Directive[AuthPayloadWithId] = {
    val task = idToken.fold(validator.process(token))(validator.parProcess(token, _))

    onComplete(task.unsafeToFuture()).flatMap {
      case Success(r) => handleValidationResponse(r).map(payload => (cId, payload))
      case Failure(_) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An unexpected error occurred"))
    }
  }

  /** Handles the success/failure of the token validation. */
  private def handleValidationResponse(response: Either[KeycloakException, AuthPayload]): Directive1[AuthPayload] = response match {
    case Right(r) => provide(r)
    case Left(t)  => complete(HttpResponse(status = t.code, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, t.getMessage)))
  }
}

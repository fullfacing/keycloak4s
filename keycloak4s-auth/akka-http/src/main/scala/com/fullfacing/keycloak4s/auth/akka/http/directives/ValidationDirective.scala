package com.fullfacing.keycloak4s.auth.akka.http.directives

import java.util.UUID

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, extractCredentials, onComplete, optionalHeaderValueByName, provide}
import com.fullfacing.keycloak4s.auth.akka.http.models.{Permissions, ResourceRoles, ValidationResult}
import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

import scala.util.{Failure, Success, Try}

trait ValidationDirective {

  /**
   * Token Validation directive to secure all inner directives.
   * Extracts the token and has it validated by the implicit instance of the TokenValidator.
   * The resource_access field is extracted from the token and provided for authorisation on
   * on inner directives.
   *
   * @return  Directive with verified user's permissions
   */
  def validateToken()(implicit tv: TokenValidator, cId: UUID): Directive1[Permissions] = {
    optionalHeaderValueByName("Id-Token").flatMap { idToken =>
      extractCredentials.flatMap {
        case Some(token)  => callValidation(token.token(), idToken)
        case None         => complete(HttpResponse(StatusCodes.Unauthorized, entity = "No token provided"))
      }
    }
  }

  /** Runs the validation function. */
  private def callValidation(token: String, idToken: Option[String])(implicit validator: TokenValidator, cId: UUID): Directive1[Permissions] = {
    onComplete(validator.validate(token, idToken).unsafeToFuture()).flatMap {
      case Success(r) => handleValidationResponse(r)
      case Failure(_) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An unexpected error occurred"))
    }
  }

  /** Handles the success/failure of the token validation. */
  private def handleValidationResponse(response: Either[KeycloakException, ValidationResult]): Directive1[Permissions] = response match {
    case Right(r) => provide(getUserPermissions(r))
    case Left(t)  => complete(HttpResponse(status = t.code, entity = HttpEntity(ContentTypes.`application/json`, write(t))))
  }

  /** Gets the resource_access field from the token and parses it into the Permissions object */
  private def getUserPermissions(result: ValidationResult): Permissions = {
    val json = parse(result.tokenPayload.toString)

    val access: Map[String, ResourceRoles] = Try {
      (json \\ "resource_access").extract[Map[String, ResourceRoles]]
    }.getOrElse(Map.empty)

    Permissions(access, result.idToken)
  }
}

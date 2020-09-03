package com.fullfacing.keycloak4s.core

import cats.data.NonEmptyList
import cats.syntax.all._
import com.fullfacing.keycloak4s.core.models.KeycloakException

object Exceptions {
  val PARSE_FAILED          = KeycloakException(400, "Bad Request", "Bearer and/or ID token is malformed.".some)
  val ID_TOKEN_MISMATCH     = KeycloakException(400, "Bad Request", "ID token does not relate to the bearer token.".some)
  val PUBLIC_KEY_NOT_FOUND  = KeycloakException(401, "Unauthorized", "The public key in the bearer token does not match the server keys.".some)
  val NOT_YET_VALID         = KeycloakException(401, "Unauthorized", "The bearer token is not yet valid.".some)
  val EXPIRED               = KeycloakException(401, "Unauthorized", "The bearer token has expired.".some)
  val EXP_MISSING           = KeycloakException(401, "Unauthorized", "The bearer token lacks an expiration date.".some)
  val IAT_MISSING           = KeycloakException(401, "Unauthorized", "The bearer token lacks an 'issued at' date.".some)
  val IAT_INCORRECT         = KeycloakException(401, "Unauthorized", "The 'issued at' time of the bearer token cannot be in the future.".some)
  val ISS_MISSING           = KeycloakException(401, "Unauthorized", "The bearer token lacks an 'issuer' web address.".some)
  val ISS_INCORRECT         = KeycloakException(401, "Unauthorized", "The 'issuer' does not match the web address as specified in the Keycloak configuration.".some)
  val SIG_INVALID           = KeycloakException(401, "Unauthorized", "Bearer and/or ID token signature verification failed.".some)
  val AUTH_MISSING          = KeycloakException(403, "Forbidden", "Authorization details not included in bearer token.".some)
  val UNAUTHORIZED          = KeycloakException(403, "Forbidden", "Authorization denied.".some)
  val ID_NOT_FOUND          = KeycloakException(400, "Bad Request", "Object created but ID could not be retrieved from headers.".some)
  val ID_PARSE_FAILED       = KeycloakException(400, "Bad Request", "Object created but ID is malformed.".some)


  def RESOURCE_NOT_FOUND(`type`: String)  = KeycloakException(500, "Internal Server Error", Some(s"${`type`} could not be retrieved."))
  def CONFIG_NOT_FOUND(filename: String)  = KeycloakException(500, "Internal Server Error", s"Policy Enforcement configuration JSON file $filename not found.".some, None)
  def JWKS_SERVER_ERROR(details: String)  = KeycloakException(500, "Internal Server Error", "Public keys could not be retrieved.".some, details.some)
  def UNEXPECTED(details: String)         = KeycloakException(500, "Internal Server Error", "An unexpected error has occurred.".some, details.some)

  class ConfigInitialisationException extends Throwable

  def buildClaimsException(exceptions: NonEmptyList[KeycloakException]): KeycloakException = {
    val exceptionMessages = exceptions.toList.flatMap(_.message)
    val message = s"Claims set could not be validated due to the following reasons:\n - ${exceptionMessages.mkString("\n - ")}"

    KeycloakException(401, "Unauthorized", message.some)
  }
}

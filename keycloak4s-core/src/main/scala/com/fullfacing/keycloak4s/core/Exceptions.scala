package com.fullfacing.keycloak4s.core

import cats.implicits._
import com.fullfacing.keycloak4s.core.models.KeycloakException

object Exceptions {
  val PARSE_FAILED          = KeycloakException(400, "Bad Request", "Bearer and/or ID token is malformed.".some)
  val ID_TOKEN_MISMATCH     = KeycloakException(400, "Bad Request", "ID token does not relate to the bearer token.".some)
  val PUBLIC_KEY_NOT_FOUND  = KeycloakException(401, "Unauthorized", "The public key in the bearer token does not match the server keys.".some)
  val NOT_YET_VALID         = KeycloakException(401, "Unauthorized", "The bearer token is not yet valid.".some)
  val EXPIRED               = KeycloakException(401, "Unauthorized", "The bearer token has expired.".some)
  val SIG_INVALID           = KeycloakException(401, "Unauthorized", "Bearer and/or ID token signature verification failed.".some)
  val AUTH_MISSING          = KeycloakException(403, "Forbidden", "Authorization details not included in bearer token.".some)
  val UNAUTHORIZED          = KeycloakException(403, "Forbidden", "Authorization denied.".some)

  def JWKS_SERVER_ERROR(details: String)  = KeycloakException(500, "Internal Server Error", "Public keys could not be retrieved.".some, details.some)
  def UNEXPECTED(details: String)         = KeycloakException(500, "Internal Server Error", "An unexpected error has occurred.".some, details.some)
}

package com.fullfacing.keycloak4s.core

import cats.implicits._
import com.fullfacing.keycloak4s.core.models.KeycloakException

object Exceptions {
  val PARSE_FAILED          = KeycloakException(400, "Bad Request", "Bearer token is malformed.".some)
  val PARSE_FAILED_ID       = KeycloakException(400, "Bad Request", "ID token is malformed.".some)
  val ID_TOKEN_MISMATCH     = KeycloakException(400, "Bad Request", "ID token does not relate to the bearer token.".some)
  val PUBLIC_KEY_NOT_FOUND  = KeycloakException(401, "Unauthorized", "The public key in the bearer token does not match the server keys.".some)
  val NOT_YET_VALID         = KeycloakException(401, "Unauthorized", "The bearer token is not yet valid.".some)
  val EXPIRED               = KeycloakException(401, "Unauthorized", "The bearer token has expired.".some)
  val SIG_INVALID           = KeycloakException(401, "Unauthorized", "Bearer token signature verification failed.".some)
  val SIG_INVALID_ID        = KeycloakException(401, "Unauthorized", "ID token signature verification failed.".some)
  val AUTH_MISSING          = KeycloakException(403, "Forbidden", "Authorization details not included in bearer token.".some)
  val UNAUTHORIZED          = KeycloakException(403, "Forbidden", "Authorization denied.".some)
  val JWKS_SERVER_ERROR     = KeycloakException(500, "Internal Server Error", "Public keys could not be retrieved.".some)
  val UNEXPECTED            = KeycloakException(500, "Internal Server Error", "An unexpected error has occurred.".some)

  /* Exception Builders that include the detailMessage of the compiler-generated Throwable. **/
  def PARSE_FAILED(details: String)          = KeycloakException(400, "Bad Request", "Bearer token is malformed.".some, details.some)
  def PARSE_FAILED_ID(details: String)       = KeycloakException(400, "Bad Request", "ID token is malformed.".some, details.some)
  def ID_TOKEN_MISMATCH(details: String)     = KeycloakException(400, "Bad Request", "ID token does not relate to the bearer token.".some, details.some)
  def PUBLIC_KEY_NOT_FOUND(details: String)  = KeycloakException(401, "Unauthorized", "The public key in the bearer token does not match the server keys.".some, details.some)
  def NOT_YET_VALID(details: String)         = KeycloakException(401, "Unauthorized", "The bearer token is not yet valid.".some, details.some)
  def EXPIRED(details: String)               = KeycloakException(401, "Unauthorized", "The bearer token has expired.".some, details.some)
  def SIG_INVALID(details: String)           = KeycloakException(401, "Unauthorized", "Bearer token signature verification failed.".some, details.some)
  def SIG_INVALID_ID(details: String)        = KeycloakException(401, "Unauthorized", "ID token signature verification failed.".some, details.some)
  def AUTH_MISSING(details: String)          = KeycloakException(403, "Forbidden", "Authorization details not included in bearer token.".some, details.some)
  def UNAUTHORIZED(details: String)          = KeycloakException(403, "Forbidden", "Authorization denied.".some, details.some)
  def JWKS_SERVER_ERROR(details: String)     = KeycloakException(500, "Internal Server Error", "Public keys could not be retrieved.".some, details.some)
  def UNEXPECTED(details: String)            = KeycloakException(500, "Internal Server Error", "An unexpected error has occurred.".some, details.some)
}

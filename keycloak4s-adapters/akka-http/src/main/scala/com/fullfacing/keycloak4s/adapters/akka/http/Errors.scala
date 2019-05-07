package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.model.HttpResponse
import com.fullfacing.keycloak4s.adapters.akka.http.Logging.logger

object Errors {

  final case class AuthError(code: Int, status: String, message: String) extends Throwable {
    override def toString = s"$code $status - $message"
  }

  val PARSE_FAILED          = AuthError(400, "Bad Request", "Bearer token is malformed.")
  val PARSE_FAILED_ID       = AuthError(400, "Bad Request", "ID token is malformed.")
  val ID_TOKEN_MISMATCH     = AuthError(400, "Bad Request", "ID token does not relate to the bearer token.")
  val PUBLIC_KEY_NOT_FOUND  = AuthError(401, "Unauthorized", "The public key in the bearer token does not match the server keys.")
  val NOT_YET_VALID         = AuthError(401, "Unauthorized", "The bearer token is not yet valid.")
  val EXPIRED               = AuthError(401, "Unauthorized", "The bearer token has expired.")
  val SIG_INVALID           = AuthError(401, "Unauthorized", "Bearer token signature verification failed.")
  val SIG_INVALID_ID        = AuthError(401, "Unauthorized", "ID token signature verification failed.")
  val AUTH_MISSING          = AuthError(403, "Forbidden", "Authorization details not included in bearer token.")
  val UNAUTHORIZED          = AuthError(403, "Forbidden", "Authorization denied.")
  val JWKS_SERVER_ERROR     = AuthError(500, "Internal Server Error", "Public keys could not be retrieved.")
  val UNEXPECTED            = AuthError(500, "Internal Server Error", "An unexpected error has occurred.")

  def errorResponse(code: Int, message: String, log: Option[String] = None): HttpResponse = {
    logger.debug(s"Invalid Request - $code: ${log.getOrElse(message)}")
    HttpResponse(code, entity = message)
  }
}

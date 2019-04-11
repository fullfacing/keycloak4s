package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.model.HttpResponse
import com.fullfacing.keycloak4s.adapters.akka.http.Logging.logger

object Errors {

  val PARSE_FAILED          = new Throwable("400 Bad Request - Bearer token is malformed.")
  val PUBLIC_KEY_NOT_FOUND  = new Throwable("401 Unauthorized - The public key in the bearer token does not match the server keys.")
  val NOT_YET_VALID         = new Throwable("401 Unauthorized - The bearer token is not yet valid.")
  val EXPIRED               = new Throwable("401 Unauthorized - The bearer token has expired.")
  val SIG_INVALID           = new Throwable("401 Unauthorized - Bearer token signature verification failed.")
  val AUTH_MISSING          = new Throwable("403 Forbidden - Authorization details not included in bearer token.")
  val UNAUTHORIZED          = new Throwable("403 Forbidden - Authorization denied.")
  val JWKS_SERVER_ERROR     = new Throwable("500 Internal Server Error - Public keys could not be retrieved.")
  val UNEXPECTED            = new Throwable("500 Internal Server Error - An unexpected error has occurred.")

  def errorResponse(code: Int, message: String, log: Option[String] = None): HttpResponse = {
    logger.debug(s"Invalid Request - $code: ${log.getOrElse(message)}")
    HttpResponse(code, entity = message)
  }
}

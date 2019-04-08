package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.model.HttpResponse

object Errors {

  val JWKS_SERVER_ERROR     = new Throwable("500 Internal Server Error - Public keys could not be retrieved.")
  val PUBLIC_KEY_NOT_FOUND  = new Throwable("401 Unauthorized - The bearer token does not contain the right public key.")
  val NOT_YET_VALID         = new Throwable("401 Unauthorized - The bearer token is not yet valid.")
  val EXPIRED               = new Throwable("401 Unauthorized - The bearer token has expired.")
  val SIG_INVALID           = new Throwable("401 Unauthorized - Bearer token signature verification failed.")

  def errorResponse(code: Int, message: String, log: Option[String] = None): HttpResponse = {
    logger.debug(s"Invalid Request - $code: ${log.getOrElse(message)}")
    HttpResponse(code, entity = message)
  }
}

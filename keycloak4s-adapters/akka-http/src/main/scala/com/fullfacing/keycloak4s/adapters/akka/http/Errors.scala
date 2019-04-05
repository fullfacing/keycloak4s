package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.model.HttpResponse

object Errors {
  val PUBLIC_KEY_NOT_FOUND = new Throwable("The bearer token does not contain the right public key.")
  val NOT_YET_VALID = new Throwable("The bearer token is not yet valid.")
  val EXPIRED = new Throwable("The bearer token has expired.")
  val SIG_INVALID = new Throwable("Bearer token signature verification failed.")

  def errorResponse(code: Int, message: String): HttpResponse = {
    logger.debug(s"Invalid Request - $code: $message")
    HttpResponse(code, entity = message)
  }
}

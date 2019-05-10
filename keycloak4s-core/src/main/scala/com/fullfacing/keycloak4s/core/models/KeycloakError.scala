package com.fullfacing.keycloak4s.core.models

sealed trait KeycloakError extends Throwable

final case class KeycloakThrowable(ex: Throwable) extends KeycloakError

/**
 * An exception processed into an HTTP error response equivalent.
 *
 * @param code    The response code.
 * @param status  The meaning of the code.
 * @param message An optional message describing the exception in greater detail.
 */
final case class KeycloakException(code: Int,
                                   status: String,
                                   message: Option[String]) extends KeycloakError {

  override def toString = s"$code $status${message.fold("")("- " + _)}"
}

/**
 * An exception returned from an STTP client.
 *
 * @param code        The status code returned.
 * @param body        The body of the response.
 * @param status      The status text returned.
 * @param headers     The headers of the response.
 * @param requestInfo Contains information of a HTTP request.
 */
final case class KeycloakSttpException(code: Int,
                                       body: String,
                                       statusText: String,
                                       headers: Seq[(String, String)],
                                       requestInfo: RequestInfo) extends KeycloakError {

  override def toString: String = {
    val requestBody = requestInfo.body.fold("")(b => s"\n|Request Body: ${b.toString}")

    s"""STTP CLIENT ERROR: $code $statusText
       |Headers: ${headers.toMap}
       |Body: $body
       |Request Endpoint: ${requestInfo.protocol} ${requestInfo.path}$requestBody""".stripMargin
  }
}
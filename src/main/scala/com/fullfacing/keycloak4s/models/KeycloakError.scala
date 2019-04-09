package com.fullfacing.keycloak4s.models

final case class RequestInfo(path: String,
                             protocol: String,
                             body: Option[Any] = None)

sealed trait KeycloakError extends Throwable

final case class KeycloakAdminException(code: Int,
                                        body: String,
                                        headers: Seq[(String, String)],
                                        statusText: String,
                                        requestInfo: RequestInfo) extends KeycloakError {

  override def toString: String = {
    val requestBody = requestInfo.body.fold("")(b => s"\nRequest Body: ${b.toString}")

    s"""STTP CLIENT ERROR: $code $statusText
       |Headers: ${headers.toMap}
       |Body: $body
       |Request Endpoint: ${requestInfo.protocol} ${requestInfo.path}""".stripMargin + requestBody
  }
}

final case class KeycloakException(ex: Throwable) extends KeycloakError
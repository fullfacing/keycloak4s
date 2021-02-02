package com.fullfacing.keycloak4s.admin.utils

import com.fullfacing.keycloak4s.core.models.{KeycloakSttpException, RequestInfo}
import sttp.client.{NoBody, Response}
import sttp.model.Uri

object Client {

  def buildRequestInfo(path: Uri, protocol: String, body: Any): RequestInfo = {
    buildRequestInfo(path.toString(), protocol, body)
  }

  def buildRequestInfo(path: Seq[String], protocol: String, body: Any): RequestInfo = {
    buildRequestInfo(path.mkString("/"), protocol, body)
  }

  def buildRequestInfo(path: String, protocol: String, body: Any): RequestInfo = {
    RequestInfo(
      path      = path,
      protocol  = protocol,
      body      = body match {
        case _: Unit  => None
        case NoBody   => None
        case a        => Some(a)
      }
    )
  }

  def buildError(response: Response[_], leftBody: String, requestInfo: RequestInfo): KeycloakSttpException = {
    KeycloakSttpException(
      code        = response.code.code,
      headers     = response.headers.map(h => h.name -> h.value),
      body        = leftBody,
      statusText  = response.statusText,
      requestInfo = requestInfo
    )
  }
}

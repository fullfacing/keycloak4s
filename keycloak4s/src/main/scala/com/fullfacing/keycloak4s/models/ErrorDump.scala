package com.fullfacing.keycloak4s.models

case class ErrorDump(code: Int,
                     body: String,
                     headers: Seq[(String, String)],
                     rawBody: String,
                     statusText: String) extends Throwable {

  override def toString: String = {
    s"""STTP CLIENT ERROR: $code $statusText
       |Headers: ${headers.toMap}
       |Body: $body
       |Raw Body: $rawBody""".stripMargin
  }
}
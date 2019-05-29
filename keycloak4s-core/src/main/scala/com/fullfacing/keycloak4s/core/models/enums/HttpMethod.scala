package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class HttpMethod(val value: String) extends StringEnumEntry

case object HttpMethods extends StringEnum[HttpMethod] {
  case object Connect extends HttpMethod("CONNECT")
  case object Delete  extends HttpMethod("DELETE")
  case object Get     extends HttpMethod("GET")
  case object Head    extends HttpMethod("HEAD")
  case object Options extends HttpMethod("OPTIONS")
  case object Patch   extends HttpMethod("PATCH")
  case object Post    extends HttpMethod("POST")
  case object Put     extends HttpMethod("PUT")
  case object Trace   extends HttpMethod("TRACE")

  val values: immutable.IndexedSeq[HttpMethod] = findValues
}
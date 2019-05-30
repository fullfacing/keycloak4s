package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Method(val value: String) extends StringEnumEntry

case object Methods extends StringEnum[Method] {
  case object All     extends Method("*")
  case object Connect extends Method("CONNECT")
  case object Delete  extends Method("DELETE")
  case object Get     extends Method("GET")
  case object Head    extends Method("HEAD")
  case object Options extends Method("OPTIONS")
  case object Patch   extends Method("PATCH")
  case object Post    extends Method("POST")
  case object Put     extends Method("PUT")
  case object Trace   extends Method("TRACE")

  val values: immutable.IndexedSeq[Method] = findValues
}
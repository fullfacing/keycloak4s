package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Direction(val value: String) extends StringEnumEntry

case object Directions extends StringEnum[Direction] {
  case object ToKeycloak    extends Direction("fedToKeycloak")
  case object ToFederation  extends Direction("keycloakToFed")

  val values: immutable.IndexedSeq[Direction] = findValues
}

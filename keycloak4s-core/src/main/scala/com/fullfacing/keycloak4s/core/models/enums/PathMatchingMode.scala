package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class PathMatchingMode extends EnumEntry

case object PathMatchingModes extends Enum[PathMatchingMode] {
  case object Full      extends PathMatchingMode
  case object Unmatched extends PathMatchingMode

  val values: immutable.IndexedSeq[PathMatchingMode] = findValues
}

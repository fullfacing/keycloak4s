package com.fullfacing.keycloak4s.models.enums

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait PolicyType extends EnumEntry

case object PolicyTypes extends Enum[PolicyType] {
  case object SKIP      extends PolicyType
  case object OVERWRITE extends PolicyType
  case object FAIL      extends PolicyType

  val values: immutable.IndexedSeq[PolicyType] = findValues
}

package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class PolicyType(val value: String) extends StringEnumEntry

case object PolicyTypes extends StringEnum[PolicyType] {
  case object Skip      extends PolicyType("SKIP")
  case object Overwrite extends PolicyType("OVERWRITE")
  case object Fail      extends PolicyType("FAIL")

  val values: immutable.IndexedSeq[PolicyType] = findValues
}

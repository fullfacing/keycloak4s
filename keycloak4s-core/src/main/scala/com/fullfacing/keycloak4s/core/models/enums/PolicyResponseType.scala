package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class PolicyResponseType(val value: String) extends StringEnumEntry

case object PolicyResponseTypes extends StringEnum[PolicyResponseType] {
  case object Skipped extends PolicyResponseType("SKIPPED")

  case object Overwritten extends PolicyResponseType("OVERWRITTEN")

  case object Added extends PolicyResponseType("ADDED")

  val values: immutable.IndexedSeq[PolicyResponseType] = findValues
}

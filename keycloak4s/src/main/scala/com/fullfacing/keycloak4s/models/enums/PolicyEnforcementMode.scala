package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class PolicyEnforcementMode(val value: String) extends StringEnumEntry

case object PolicyEnforcementModes extends StringEnum[PolicyEnforcementMode] {
  case object Enforcing   extends PolicyEnforcementMode("ENFORCING")
  case object Permissive  extends PolicyEnforcementMode("PERMISSIVE")
  case object Disabled    extends PolicyEnforcementMode("DISABLED")

  val values: immutable.IndexedSeq[PolicyEnforcementMode] = findValues
}

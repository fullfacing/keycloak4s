package com.fullfacing.keycloak4s.models.enums

import enumeratum.Enum
import enumeratum.EnumEntry

import scala.collection.immutable

sealed trait PolicyEnforcementMode extends EnumEntry

case object PolicyEnforcementModes extends Enum[PolicyEnforcementMode] {
  case object ENFORCING   extends PolicyEnforcementMode
  case object PERMISSIVE  extends PolicyEnforcementMode
  case object DISABLED    extends PolicyEnforcementMode

  val values: immutable.IndexedSeq[PolicyEnforcementMode] = findValues
}

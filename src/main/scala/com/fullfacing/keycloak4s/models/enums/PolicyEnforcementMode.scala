package com.fullfacing.keycloak4s.models.enums
import enumeratum._

sealed trait PolicyEnforcementMode extends EnumEntry

object PolicyEnforcementMode extends Enum[PolicyEnforcementMode] {
  val values = findValues

  case object ENFORCING extends PolicyEnforcementMode

  case object PERMISSIVE extends PolicyEnforcementMode

  case object DISABLED extends PolicyEnforcementMode
}

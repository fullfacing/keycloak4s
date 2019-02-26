package com.fullfacing.keycloak4s.models.enums
import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait PolicyEnforcementMode extends EnumEntry

object PolicyEnforcementMode extends Enum[PolicyEnforcementMode] {
  override def values: IndexedSeq[PolicyEnforcementMode] = findValues

  case object ENFORCING   extends PolicyEnforcementMode
  case object PERMISSIVE  extends PolicyEnforcementMode
  case object DISABLED    extends PolicyEnforcementMode
}

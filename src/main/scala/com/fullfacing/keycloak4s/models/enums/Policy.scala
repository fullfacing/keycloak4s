package com.fullfacing.keycloak4s.models.enums
import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait Policy extends EnumEntry

object Policy extends Enum[Policy] {
  override def values: IndexedSeq[Policy] = findValues

  case object SKIP      extends Policy
  case object OVERWRITE extends Policy
  case object FAIL      extends Policy
}

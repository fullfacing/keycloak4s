package com.fullfacing.keycloak4s.models.enums
import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait Logic extends EnumEntry

object Logic extends Enum[Logic] {
  override def values: IndexedSeq[Logic] = findValues

  case object POSITIVE extends Logic
  case object NEGATIVE extends Logic
}



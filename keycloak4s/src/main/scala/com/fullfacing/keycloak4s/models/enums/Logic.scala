package com.fullfacing.keycloak4s.models.enums

import enumeratum.Enum
import enumeratum.EnumEntry

import scala.collection.immutable

sealed trait Logic extends EnumEntry

case object LogicTypes extends Enum[Logic] {
  case object POSITIVE  extends Logic
  case object NEGATIVE  extends Logic

  val values: immutable.IndexedSeq[Logic] = findValues
}



package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Logic(val value: String) extends StringEnumEntry

case object LogicTypes extends StringEnum[Logic] {
  case object Positive  extends Logic("POSITIVE")
  case object Negative  extends Logic("NEGATIVE")

  val values: immutable.IndexedSeq[Logic] = findValues
}



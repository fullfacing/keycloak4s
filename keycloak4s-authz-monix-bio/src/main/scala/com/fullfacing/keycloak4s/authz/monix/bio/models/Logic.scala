package com.fullfacing.keycloak4s.authz.monix.bio.models

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Logic(val value: String) extends StringEnumEntry

case object Logic extends StringEnum[Logic] {

  case object Positive extends Logic("POSITIVE")
  case object Negative extends Logic("NEGATIVE")

  def values: immutable.IndexedSeq[Logic] = findValues
}

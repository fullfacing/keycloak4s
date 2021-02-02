package com.fullfacing.keycloak4s.authz.models

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class Logic(val value: String) extends StringEnumEntry

case object Logic extends StringEnum[Logic] {

  case object Positive extends Logic("POSITIVE")
  case object Negative extends Logic("NEGATIVE")

  def values: IndexedSeq[Logic] = findValues
}

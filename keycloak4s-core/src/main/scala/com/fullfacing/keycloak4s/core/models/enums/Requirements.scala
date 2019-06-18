package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Requirement(val value: String) extends StringEnumEntry

case object Requirements extends StringEnum[Requirement] {
  case object Enabled     extends Requirement("ENABLED")
  case object Disabled    extends Requirement("DISABLED")
  case object Required    extends Requirement("REQUIRED")
  case object Optional    extends Requirement("OPTIONAL")
  case object Alternative extends Requirement("ALTERNATIVE")

  val values: immutable.IndexedSeq[Requirement] = findValues
}

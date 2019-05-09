package com.fullfacing.keycloak4s.admin.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Category(val value: String) extends StringEnumEntry

case object Categories extends StringEnum[Category] {
  case object Internal  extends Category("INTERNAL")
  case object Access    extends Category("ACCESS")
  case object Id        extends Category("ID")
  case object Admin     extends Category("ADMIN")
  case object UserInfo  extends Category("USERINFO")

  val values: immutable.IndexedSeq[Category] = findValues
}

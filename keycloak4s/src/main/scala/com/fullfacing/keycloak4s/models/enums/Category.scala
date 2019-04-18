package com.fullfacing.keycloak4s.models.enums

import enumeratum.Enum
import enumeratum.EnumEntry

import scala.collection.immutable

sealed trait Category extends EnumEntry

case object Categories extends Enum[Category] {
  case object INTERNAL  extends Category
  case object ACCESS    extends Category
  case object ID        extends Category
  case object ADMIN     extends Category
  case object USERINFO  extends Category

  val values: immutable.IndexedSeq[Category] = findValues
}

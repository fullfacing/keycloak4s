package com.fullfacing.keycloak4s.models.enums
import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait Category extends EnumEntry

object Category extends Enum[Category] {
  override def values: IndexedSeq[Category] = findValues

  case object INTERNAL  extends Category
  case object ACCESS    extends Category
  case object ID        extends Category
  case object ADMIN     extends Category
  case object USERINFO  extends Category
}

package models.enums
import enumeratum._

sealed trait Category extends EnumEntry

object Category extends Enum[Category] {
  val values = findValues

  case object INTERNAL extends Category

  case object ACCESS extends Category

  case object ID extends Category

  case object ADMIN extends Category

  case object USERINFO extends Category
}

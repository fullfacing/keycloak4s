package models.enums
import enumeratum._

sealed trait Policy extends EnumEntry

object Policy extends Enum[Policy] {
  val values = findValues

  case object SKIP extends Policy

  case object OVERWRITE extends Policy

  case object FAIL extends Policy
}

package models.enums
import enumeratum._

sealed trait Logic extends EnumEntry

object Logic extends Enum[Logic] {
  val values = findValues

  case object POSITIVE extends Logic

  case object NEGATIVE extends Logic
}



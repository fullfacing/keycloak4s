package models.enums
import enumeratum._

sealed trait DecisionStrategy extends EnumEntry

object DecisionStrategy extends Enum[DecisionStrategy] {
  val values = findValues

  case object AFFIRMATIVE extends DecisionStrategy

  case object UNANIMOUS extends DecisionStrategy

  case object CONSENSUS extends DecisionStrategy
}
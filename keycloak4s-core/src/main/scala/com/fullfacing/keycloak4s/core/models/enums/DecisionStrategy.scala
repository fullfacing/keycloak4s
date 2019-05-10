package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class DecisionStrategy(val value: String) extends StringEnumEntry

case object DecisionStrategies extends StringEnum[DecisionStrategy] {
  case object Affirmative extends DecisionStrategy("AFFIRMATIVE")
  case object Unanimous   extends DecisionStrategy("UNANIMOUS")
  case object Consensus   extends DecisionStrategy("CONSENSUS")

  val values: immutable.IndexedSeq[DecisionStrategy] = findValues
}
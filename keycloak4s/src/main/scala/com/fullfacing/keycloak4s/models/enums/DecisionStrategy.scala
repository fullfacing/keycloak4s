package com.fullfacing.keycloak4s.models.enums

import enumeratum.Enum
import enumeratum.EnumEntry

import scala.collection.immutable

sealed trait DecisionStrategy extends EnumEntry

case object DecisionStrategies extends Enum[DecisionStrategy] {
  case object AFFIRMATIVE extends DecisionStrategy
  case object UNANIMOUS   extends DecisionStrategy
  case object CONSENSUS   extends DecisionStrategy

  val values: immutable.IndexedSeq[DecisionStrategy] = findValues
}
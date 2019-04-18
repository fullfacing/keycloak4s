package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class TriggerSyncAction(val value: String) extends StringEnumEntry

case object TriggerSyncActions extends StringEnum[TriggerSyncAction] {
  case object FullSync          extends TriggerSyncAction("triggerFullSync")
  case object ChangedUsersSync  extends TriggerSyncAction("triggerChangedUsersSync")

  val values: immutable.IndexedSeq[TriggerSyncAction] = findValues
}

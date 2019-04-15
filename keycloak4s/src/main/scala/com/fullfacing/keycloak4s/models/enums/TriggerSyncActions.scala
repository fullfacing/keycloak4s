package com.fullfacing.keycloak4s.models.enums

/** Used by UserStorageProvider.syncUsers */
object TriggerSyncActions {
  val triggerFullSync = "triggerFullSync"
  val triggerChangedUsersSync = "triggerChangedUsersSync"
}

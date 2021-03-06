package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.enums.{Direction, TriggerSyncAction}
import com.fullfacing.keycloak4s.core.models.{KeycloakError, SimpleNameResponse, Synchronization}

import scala.collection.immutable.Seq

class UserStorageProviders[R[+_]: Concurrent](implicit client: KeycloakClient[R]) {

  /** Need this for admin console to display simple name of provider when displaying user detail. */
  def userSimpleProviderName(userStorageId: String): R[Either[KeycloakError, SimpleNameResponse]] = {
    val path = Seq(client.realm, "user-storage", userStorageId, "name")
    client.get[SimpleNameResponse](path)
  }

  /** Delete imported users */
  def deleteImportedUsers(userStorageId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "user-storage", userStorageId, "remove-imported-users")
    client.post[Unit](path)
  }

  /** Trigger sync of users. */
  def syncUsers(userStorageId: String, action: Option[TriggerSyncAction]): R[Either[KeycloakError, Synchronization]] = {
    val path  = Seq(client.realm, "user-storage", userStorageId, "sync")
    val query = createQuery(("action", action.map(_.value)))
    client.post[Synchronization](path, query = query)
  }

  /** Unlink imported users from a storage provider */
  def unlink(userStorageId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "user-storage", userStorageId, "unlink-users")
    client.post[Unit](path)
  }

  /** Trigger sync of mapper data related to ldap mapper (roles, groups, …​) */
  def syncMapperData(mapperId: String, userStorageId: String, direction: Option[Direction]): R[Either[KeycloakError, Synchronization]] = {
    val path  = Seq(client.realm, "user-storage", userStorageId, "mappers", mapperId, "sync")
    val query = createQuery(("direction", direction.map(_.value)))
    client.post[Synchronization](path, query = query)
  }
}

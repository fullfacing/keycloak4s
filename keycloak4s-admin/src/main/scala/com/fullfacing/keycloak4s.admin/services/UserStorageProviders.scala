package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.enums.{Direction, TriggerSyncAction}
import com.fullfacing.keycloak4s.core.models.{SimpleNameResponse, Synchronization}
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class UserStorageProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val user_storage = "user-storage"

  // TODO Note: API docs don't specify any base path as with below call, must be confirmed. Cannot find endpoint specified in keycloak github repo.
  // The description in the documentation includes a ticket number, however said ticket has been closed. The docs may be out of date for this section.
  /**
   * Need this for admin console to display simple name of provider when displaying client detail
   *
   * @param id
   * @return
   */
  def clientSimpleProviderName(id: String): R[Either[KeycloakError, Map[String, Any]]] = {
    val path = Seq(id, "name")
    client.get[Map[String, Any]](path)
  }

  /**
   * Need this for admin console to display simple name of provider when displaying user detail
   *
   * @param userStorageId
   * @return
   */
  def userSimpleProviderName(userStorageId: String): R[Either[KeycloakError, SimpleNameResponse]] = {
    val path = Seq(client.realm, user_storage, userStorageId, "name")
    client.get[SimpleNameResponse](path)
  }

  /**
   * Remove imported users
   * @param userStorageId
   * @return
   */
  def removeImportedUsers(userStorageId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, user_storage, userStorageId, "remove-imported-users")
    client.post[Unit](path)
  }

  /**
   * Trigger sync of users
   * Action can be "triggerFullSync" or "triggerChangedUsersSync"
   *
   * @param userStorageId
   * @param action  com.fullfacing.keycloak4s.models.enums.TriggerSyncActions
   * @return
   */
  def syncUsers(userStorageId: String, action: Option[TriggerSyncAction]): R[Either[KeycloakError, Synchronization]] = {
    val path  = Seq(client.realm, user_storage, userStorageId, "sync")
    val query = createQuery(("action", action.map(_.value)))
    client.post[Synchronization](path, query = query)
  }

  /**
   * Unlink imported users from a storage provider
   *
   * @param userStorageId
   * @return
   */
  def unlink(userStorageId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, user_storage, userStorageId, "unlink-users")
    client.post[Unit](path)
  }

  /**
   * Trigger sync of mapper data related to ldap mapper (roles, groups, …​)
   * Direction is "fedToKeycloak" or "keycloakToFed"
   *
   * @param mapperId
   * @param userStorageId
   * @param direction com.fullfacing.keycloak4s.models.enums.MapperSyncDirections
   * @return
   */
  def syncMapperData(mapperId: String, userStorageId: String, direction: Option[Direction]): R[Either[KeycloakError, Synchronization]] = {
    val path  = Seq(client.realm, user_storage, userStorageId, "mappers", mapperId, "sync")
    val query = createQuery(("direction", direction.map(_.value)))
    client.post[Synchronization](path, query = query)
  }
}

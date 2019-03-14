package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{SimpleNameResponse, Synchronization}

import scala.collection.immutable.Seq

class UserStorageProviders[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val user_storage = "user-storage"

  // TODO Note: API docs don't specify any base path as with below call, must be confirmed. Cannot find endpoint specified in keycloak github repo.
  // The description in the documentation includes a ticket number, however said ticket has been closed. The docs may be out of date for this section.
  /**
   * Need this for admin console to display simple name of provider when displaying client detail
   *
   * @param id
   * @return
   */
  def clientSimpleProviderName(id: String): R[Map[String, Any]] = {
    val path = Seq(id, "name")
    client.get[Map[String, Any]](path)
  }

  /**
   * Need this for admin console to display simple name of provider when displaying user detail
   *
   * @param realm
   * @param userStorageId
   * @return
   */
  def userSimpleProviderName(realm: String, userStorageId: String): R[SimpleNameResponse] = {
    val path = Seq(realm, user_storage, userStorageId, "name")
    client.get[SimpleNameResponse](path)
  }

  /**
   * Remove imported users
   *
   * @param realm realm name (not id!)
   * @param userStorageId
   * @return
   */
  def removeImportedUsers(realm: String, userStorageId: String): R[Unit] = {
    val path = Seq(realm, user_storage, userStorageId, "remove-imported-users")
    client.post(path)
  }

  /**
   * Trigger sync of users
   * Action can be "triggerFullSync" or "triggerChangedUsersSync"
   *
   * @param realm
   * @param userStorageId
   * @param action  com.fullfacing.keycloak4s.models.enums.TriggerSyncActions
   * @return
   */
  def syncUsers(realm: String, userStorageId: String, action: Option[String]): R[Synchronization] = {
    val path  = Seq(realm, user_storage, userStorageId, "sync")
    val query = createQuery(("action", action))
    client.post[Unit, Synchronization](path, query = query)
  }

  /**
   * Unlink imported users from a storage provider
   *
   * @param realm
   * @param userStorageId
   * @return
   */
  def unlink(realm: String, userStorageId: String): R[Unit] = {
    val path = Seq(realm, user_storage, userStorageId, "unlink-users")
    client.post(path)
  }

  /**
   * Trigger sync of mapper data related to ldap mapper (roles, groups, …​)
   * Direction is "fedToKeycloak" or "keycloakToFed"
   *
   * @param realm
   * @param mapperId
   * @param userStorageId
   * @param direction com.fullfacing.keycloak4s.models.enums.MapperSyncDirections
   * @return
   */
  def syncMapperData(realm: String, mapperId: String, userStorageId: String, direction: Option[String]): R[Synchronization] = {
    val path  = Seq(realm, user_storage, userStorageId, "mappers", mapperId, "sync")
    val query = createQuery(("direction", direction))
    client.post[Unit, Synchronization](path, query = query)
  }
}

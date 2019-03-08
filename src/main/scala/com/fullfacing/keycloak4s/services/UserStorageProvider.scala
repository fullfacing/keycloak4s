package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.Synchronization

import scala.collection.immutable.Seq

class UserStorageProvider[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val user_storage = "user-storage"

  /**
   * Need this for admin console to display simple name of provider when displaying client detail KEYCLOAK-4328
   *
   * @param id
   * @return
   */
  def clientSimpleProviderName(id: String): R[Map[String, Any]] = {
    val path = Seq(id, "name")
    client.get[Map[String, Any]](path)
  }

  /**
   * Need this for admin console to display simple name of provider when displaying user detail KEYCLOAK-4328
   *
   * @param realm
   * @param id
   * @return
   */
  def userSimpleProviderName(realm: String, id: String): R[Map[String, Any]] = { // TODO
    val path = Seq(realm, user_storage, id, "name")
    client.get[Map[String, Any]](path)
  }

  /**
   * Remove imported users
   *
   * @param realm
   * @param id
   * @return
   */
  def removeImportedUser(realm: String, id: String): R[Unit] = {
    val path = Seq(realm, user_storage, id, "remove-imported-users")
    client.postNoContent(path)
  }

  /**
   * Trigger sync of users Action can be "triggerFullSync" or "triggerChangedUsersSync"
   *
   * @param realm
   * @param id
   * @param action
   * @return
   */
  def sync(realm: String, id: String, action: Option[String]): R[Synchronization] = {
    val path  = Seq(realm, user_storage, id, "sync")
    val query = createQuery(("action", action))
    client.post[Synchronization](path, query)
  }

  /**
   * Unlink imported users from a storage provider
   *
   * @param realm
   * @param id
   * @return
   */
  def unlink(realm: String, id: String): R[Unit] = {
    val path = Seq(realm, user_storage, id, "unlink-users")
    client.postNoContent(path)
  }

  /**
   * Trigger sync of mapper data related to ldap mapper (roles, groups, …​) direction is "fedToKeycloak" or "keycloakToFed"
   *
   * @param realm
   * @param id
   * @param parentId
   * @param direction
   * @return
   */
  def mapperDataSync(realm: String, id: String, parentId: String, direction: Option[String]): R[Synchronization] = {
    val path  = Seq(realm, user_storage, parentId, "mappers", id, "sync")
    val query = createQuery(("direction", direction))
    client.post[Synchronization](path, query)
  }
}

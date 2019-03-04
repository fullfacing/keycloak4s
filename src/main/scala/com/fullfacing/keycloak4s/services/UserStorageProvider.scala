package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.handles.SttpClient.UnknownMap
import com.fullfacing.keycloak4s.handles.SttpClient
import com.fullfacing.keycloak4s.models.Synchronization
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object UserStorageProvider {

  private val resource = "user-storage"

  /**
   * Need this for admin console to display simple name of provider when displaying client detail KEYCLOAK-4328
   *
   * @param id
   * @return
   */
  def clientSimpleProviderName(id: String)(implicit authToken: String): AsyncApolloResponse[UnknownMap] = {
    val path = Seq(id, "name")
    SttpClient.get(path)
  }

  /**
   * Need this for admin console to display simple name of provider when displaying user detail KEYCLOAK-4328
   *
   * @param realm
   * @param id
   * @return
   */
  def userSimpleProviderName(realm: String, id: String)(implicit authToken: String): AsyncApolloResponse[UnknownMap] = { // TODO
    val path = Seq(realm, resource, id, "name")
    SttpClient.get(path)
  }

  /**
   * Remove imported users
   *
   * @param realm
   * @param id
   * @return
   */
  def removeImportedUser(realm: String, id: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, resource, id, "remove-imported-users")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Trigger sync of users Action can be "triggerFullSync" or "triggerChangedUsersSync"
   *
   * @param realm
   * @param id
   * @param action
   * @return
   */
  def sync(realm: String, id: String, action: Option[String])(implicit authToken: String): AsyncApolloResponse[Synchronization] = {
    val path  = Seq(realm, resource, id, "sync")
    val query = createQuery(("action", action))
    SttpClient.post(path, query)
  }

  /**
   * Unlink imported users from a storage provider
   *
   * @param realm
   * @param id
   * @return
   */
  def unlink(realm: String, id: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, resource, id, "unlink-users")
    SttpClient.post(path, Seq.empty[KeyValue])
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
  def mapperDataSync(realm: String, id: String, parentId: String, direction: Option[String])(implicit authToken: String): AsyncApolloResponse[Synchronization] = {
    val path  = Seq(realm, resource, parentId, "mappers", id, "sync")
    val query = createQuery(("direction", direction))
    SttpClient.post(path, query)
  }
}

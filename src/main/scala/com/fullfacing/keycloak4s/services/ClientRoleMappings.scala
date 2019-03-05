package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.handles.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object ClientRoleMappings {

  //TODO Official documentation for ClientRoleMappings is lacking in detail and does not specify which "id" is required and if "client" is an ID or name.

  /**
   * Add client-level roles to the group role mapping.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @param roles
   * @return
   */
  def addRolesToGroup(client: String, id: String, realm: String, roles: Seq[Role])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
    SttpClient.post(roles, path)
  }

  /**
   * Get client-level role mappings for the group.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getGroupRoleMappings(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
    SttpClient.get(path)
  }

  /**
   * Delete client-level roles from group role mapping.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @param roles
   * @return
   */
  def deleteGroupRoles(client: String, id: String, realm: String, roles: Seq[Role])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
    SttpClient.delete(roles, path, Seq.empty[KeyValue])
  }

  /**
   * Get available client-level roles that can be mapped to the group.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getAvailableGroupRoles(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client, "available")
    SttpClient.get(path)
  }

  /**
   * Get effective client-level group role mappings.
   * This recurses any composite roles.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getEffectiveGroupRoles(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client, "composite")
    SttpClient.get(path)
  }

  /**
   * Add client-level roles to the user role mapping.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @param roles
   * @return
   */
  def addRolesToUser(client: String, id: String, realm: String, roles: Seq[Role])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "users", id, "role-mapping", "clients", client)
    SttpClient.post(roles, path)
  }

  /**
   * Get client-level role mappings for the user.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getUserRoleMappings(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "users", id, "role-mapping", "clients", client)
    SttpClient.get(path)
  }

  /**
   * Delete client-level roles from user role mapping.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @param roles
   * @return
   */
  def deleteUserRoles(client: String, id: String, realm: String, roles: Seq[Role])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
    SttpClient.delete(roles, path, Seq.empty[KeyValue])
  }

  /**
   * Get available client-level roles that can be mapped to the user.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getAvailableUserRoles(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "users", id, "role-mapping", "clients", client, "available")
    SttpClient.get(path)
  }

  /**
   * Get effective client-level user role mappings.
   * This recurses any composite roles.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @return
   */
  def getEffectiveUserRoles(client: String, id: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "users", id, "role-mapping", "clients", client, "composite")
    SttpClient.get(path)
  }
}

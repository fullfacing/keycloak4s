package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object ClientRoleMappings {

  //TODO Official documentation for ClientRoleMappings is lacking in detail and does not specify which "id" is required and if "client" is an ID or name.

  /**
   * Add client-level roles to the user role mapping.
   *
   * @param client
   * @param id
   * @param realm   Name of the Realm.
   * @param roles
   * @return
   */
  def addClientLevelRoles(client: String, id: String, realm: String, roles: Seq[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
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
  def getClientLevelRoleMappings(client: String, id: String, realm: String): AsyncApolloResponse[Seq[Role]] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
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
  def deleteClientLevelRoles(client: String, id: String, realm: String, roles: Seq[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "groups", id, "role-mapping", "clients", client)
    SttpClient.delete(roles, path)
  }
}

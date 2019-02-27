package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.{Mappings, Role}

import scala.collection.immutable.Seq

object ScopeMappings {

  private val resource = "client-scopes"
  private val mappings = "scope-mappings"

  /**
   * Get all scope mappings for the client
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @return
   */
  def fetchMappings(realm: String, id: String): AsyncApolloResponse[Mappings] = {
    val path = Seq(realm, resource, id, mappings)
    SttpClient.get(path)
  }

  /**
   * Add client-level roles to the client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param client
   * @param roles
   * @return
   */
  def addClientRoles(realm: String, id: String, client: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, resource, id, mappings, "clients", client)
    SttpClient.post(roles, path)
  }

  /**
   * Get the roles associated with a client’s scope. Returns roles for the client.
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getClientRoles(realm: String, id: String, client: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, resource, id, mappings, "clients", client)
    SttpClient.get(path)
  }

  /**
   * Remove client-level roles from the client’s scope.
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def removeClientRoles(realm: String, id: String, client: String, roles: List[Role]): AsyncApolloResponse[NoContent] = { // TODO Test - Delete with body
    val path = Seq(realm, resource, id, mappings, "clients", client)
    SttpClient.delete(roles, path)
  }

  /**
   * The available client-level roles
   * Returns the roles for the client that can be associated with the client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getAvailableClientRoles(realm: String, id: String, client: String): AsyncApolloResponse[Role] = {
    val path = Seq(realm, resource, id, mappings, "clients", client, "available")
    SttpClient.get(path)
  }

  /**
   * Get effective client roles
   * Returns the roles for the client that are associated with the client’s scope.
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getEffectiveClientRoles(realm: String, id: String, client: String): AsyncApolloResponse[Role] = {
    val path = Seq(realm, resource, id, mappings, "clients", client, "composite")
    SttpClient.get(path)
  }

  /**
   * Add a set of realm-level roles to the client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param roles
   * @return
   */
  def addRealmRolesToClientScope(realm: String, id: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, resource, id, mappings, "realm")
    SttpClient.post(roles, path)
  }

  /**
   * Get realm-level roles associated with the client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @return
   */
  def getClientScopeRealmRoles(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, resource, id, mappings, "realm")
    SttpClient.get(path)
  }

  /**
   * Remove a set of realm-level roles from the client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @param roles
   * @return
   */
  def removeRealmRolesFromClientScope(realm: String, id: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, resource, id, mappings, "realm")
    SttpClient.delete(roles, path)
  }

  /**
   * Get realm-level roles that are available to attach to this client’s scope
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @return
   */
  def getAvailableRealmLevelRoles(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, resource, id, mappings, "realm", "available")
    SttpClient.get(path)
  }

  /**
   * Get effective realm-level roles associated with the client’s scope.
   * What this does is recurse any composite roles associated with the client’s scope and adds the roles to this lists.
   *
   * The method is really to show a comprehensive total view of realm-level roles associated with the client
   *
   * @param realm realm name (not id!)
   * @param id    id of client scope (not name)
   * @return
   */
  def getEffectiveRealmLevelRoles(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, resource, id, mappings, "realm", "composite")
    SttpClient.get(path)
  }
}

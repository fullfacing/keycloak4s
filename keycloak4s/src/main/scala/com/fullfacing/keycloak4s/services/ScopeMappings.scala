package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{Mappings, Role}

import scala.collection.immutable.Seq

class ScopeMappings[R[_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  private val resource = "client-scopes"
  private val mappings = "scope-mappings"

  /**
   * Get all scope mappings for the client
   *
   * @param id    id of client scope (not name)
   * @return
   */
  def fetchMappings(id: String): R[Mappings] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings)
    keycloakClient.get[Mappings](path)
  }

  /**
   * Add client-level roles to the client’s scope
   *
   * @param id    id of client scope (not name)
   * @param client
   * @param roles
   * @return
   */
  def addClientRoles(id: String, client: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "clients", client)
    keycloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get the roles associated with a client’s scope. Returns roles for the client.
   *
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getClientRoles(id: String, client: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "clients", client)
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove client-level roles from the client’s scope.
   *
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def removeClientRoles(id: String, client: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "clients", client)
    keycloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * The available client-level roles
   * Returns the roles for the client that can be associated with the client’s scope
   *
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getAvailableClientRoles(id: String, client: String): R[Role] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "clients", client, "available")
    keycloakClient.get[Role](path)
  }

  /**
   * Get effective client roles
   * Returns the roles for the client that are associated with the client’s scope.
   *
   * @param id    id of client scope (not name)
   * @param client
   * @return
   */
  def getEffectiveClientRoles(id: String, client: String): R[Role] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "clients", client, "composite")
    keycloakClient.get[Role](path)
  }

  /**
   * Add a set of realm-level roles to the client’s scope
   *
   * @param id    id of client scope (not name)
   * @param roles
   * @return
   */
  def addRealmRolesToClientScope(id: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "realm")
    keycloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level roles associated with the client’s scope
   *
   * @param id    id of client scope (not name)
   * @return
   */
  def getClientScopeRealmRoles(id: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "realm")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove a set of realm-level roles from the client’s scope
   *
   * @param id    id of client scope (not name)
   * @param roles
   * @return
   */
  def removeRealmRolesFromClientScope(id: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "realm")
    keycloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level roles that are available to attach to this client’s scope
   *
   * @param id    id of client scope (not name)
   * @return
   */
  def getAvailableRealmLevelRoles(id: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "realm", "available")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Get effective realm-level roles associated with the client’s scope.
   * What this does is recurse any composite roles associated with the client’s scope and adds the roles to this lists.
   *
   * The method is really to show a comprehensive total view of realm-level roles associated with the client
   *
   * @param id    id of client scope (not name)
   * @return
   */
  def getEffectiveRealmLevelRoles(id: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, id, mappings, "realm", "composite")
    keycloakClient.get[List[Role]](path)
  }
}

package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.models.{Mappings, Role}
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class ScopeMappings[R[+_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  private val `client-scopes`  = "client-scopes"
  private val `scope-mappings` = "scope-mappings"

  /** Get all scope mappings for the client scope */
  def fetch(id: UUID): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`)
    keycloakClient.get[Mappings](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ----------------------------------------- Client Level Roles ----------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /**
   * Add client-level roles to the client’s scope.
   *
   * @param roleNames Names of the client level roles to be mapped to the client scope.
   */
  def addClientRoles(id: UUID, clientId: UUID, roleNames: List[String]): R[Either[KeycloakError, Unit]] = {
    val body = roleNames.map(r => Role.Mapping(name = Some(r)))
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "clients", clientId.toString)
    keycloakClient.post[Unit](path, body)
  }

  /** Get the roles associated with a client’s scope. Returns roles for the client. */
  def fetchClientRoles(id: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "clients", clientId.toString)
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove client-level roles from the client’s scope.
   *
   * @param roleNames Names of the client level roles to be removed from the client scope.
   */
  def removeClientRoles(id: UUID, clientId: UUID, roleNames: List[String]): R[Either[KeycloakError, Unit]] = {
    val body = roleNames.map(r => Role.Mapping(name = Some(r)))
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "clients", clientId.toString)
    keycloakClient.delete[Unit](path, body)
  }

  /**
   * The available client-level roles.
   * Returns the roles for the client that can be associated with the client’s scope.
   */
  def fetchAvailableClientRoles(id: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "clients", clientId.toString, "available")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Get effective client roles.
   * Returns the roles for the client that are associated with the client’s scope.
   */
  def fetchEffectiveClientRoles(id: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "clients", clientId.toString, "composite")
    keycloakClient.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ----------------------------------------- Realm Level Roles ------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  /**
   * Add a set of realm-level roles to the client’s scope
   *
   * @param id      id of client scope (not name).
   * @param roleIds IDs of the realm level roles to be mapped to the client scope.
   */
  def addRealmRoles(id: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(r => Role.Mapping(Some(r)))
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "realm")
    keycloakClient.post[Unit](path, body)
  }

  /** Get realm-level roles associated with the client’s scope. */
  def fetchRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "realm")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove a set of realm-level roles from the client’s scope
   *
   * @param id      id of client scope (not name)
   * @param roleIds IDs of the realm level roles to be removed from the client scope.
   */
  def removeRealmRoles(id: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(r => Role.Mapping(Some(r)))
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "realm")
    keycloakClient.delete[Unit](path, body)
  }

  /** Get realm-level roles that are available to attach to this client’s scope. */
  def fetchAvailableRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "realm", "available")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Get effective realm-level roles associated with the client’s scope.
   * What this does is recurse any composite roles associated with the client’s scope and adds the roles to this lists.
   *
   * The method is really to show a comprehensive total view of realm-level roles associated with the client.
   */
  def fetchEffectiveRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(keycloakClient.realm, `client-scopes`, id.toString, `scope-mappings`, "realm", "composite")
    keycloakClient.get[List[Role]](path)
  }
}

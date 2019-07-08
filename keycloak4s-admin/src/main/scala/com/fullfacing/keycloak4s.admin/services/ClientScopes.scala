package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{ClientScope, KeycloakError, Mappings, Role}

import scala.collection.immutable.Seq

class ClientScopes[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Create a new client scope. Client Scope’s name must be unique! */
  def create(clientScope: ClientScope.Create): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.post[Unit](path, clientScope)
  }

  def fetch(): R[Either[KeycloakError, List[ClientScope]]] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.get[List[ClientScope]](path)
  }

  def fetchById(scopeId: UUID): R[Either[KeycloakError, ClientScope]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.get[ClientScope](path)
  }

  def update(scopeId: UUID, clientScope: ClientScope.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.put[Unit](path, clientScope)
  }

  def delete(scopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.delete[Unit](path)
  }


  /** Get all scope mappings for the client scope */
  def fetchScopeMappings(scopeId: UUID): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings")
    client.get[Mappings](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------- Client Level Role Mappings ------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /**
   * Add client-level roles to the client’s scope.
   *
   * @param roleNames Names of the client level roles to be mapped to the client scope.
   */
  def addClientRoles(scopeId: UUID, clientId: UUID, roleNames: List[String]): R[Either[KeycloakError, Unit]] = {
    val body = roleNames.map(Name)
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "clients", clientId.toString)
    client.post[Unit](path, body)
  }

  /** Get the roles associated with a client’s scope. Returns roles for the client. */
  def fetchClientRoles(scopeId: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "clients", clientId.toString)
    client.get[List[Role]](path)
  }

  /**
   * Remove client-level roles from the client’s scope.
   *
   * @param roleNames Names of the client level roles to be removed from the client scope.
   */
  def removeClientRoles(scopeId: UUID, clientId: UUID, roleNames: List[String]): R[Either[KeycloakError, Unit]] = {
    val body = roleNames.map(Name)
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "clients", clientId.toString)
    client.delete[Unit](path, body)
  }

  /**
   * The available client-level roles.
   * Returns the roles for the client that can be associated with the client’s scope.
   */
  def fetchAvailableClientRoles(scopeId: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "clients", clientId.toString, "available")
    client.get[List[Role]](path)
  }

  /**
   * Get effective client roles.
   * Returns the roles for the client that are associated with the client’s scope.
   */
  def fetchEffectiveClientRoles(scopeId: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "clients", clientId.toString, "composite")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------- Realm Level Role mappings -------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /**
   * Add a set of realm-level roles to the client-scope
   *
   * @param scopeId ID of client scope (not name).
   * @param roleIds IDs of the realm level roles to be mapped to the client scope.
   */
  def addRealmRoles(scopeId: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(Id)
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "realm")
    client.post[Unit](path, body)
  }

  /** Get realm-level roles associated with the client-scope. */
  def fetchRealmRoles(scopeId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "realm")
    client.get[List[Role]](path)
  }

  /**
   * Remove a set of realm-level roles from the client-scope
   *
   * @param scopeId ID of client scope (not name).
   * @param roleIds IDs of the realm level roles to be removed from the client scope.
   */
  def removeRealmRoles(scopeId: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(Id)
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "realm")
    client.delete[Unit](path, body)
  }

  /** Get realm-level roles that are available to attach to this client-scope. */
  def fetchAvailableRealmRoles(scopeId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /**
   * Get effective realm-level roles associated with the client-scope.
   * What this does is recurse any composite roles associated with the client’s scope and adds the roles to this lists.
   *
   * The method is really to show a comprehensive total view of realm-level roles associated with the client.
   */
  def fetchEffectiveRealmRoles(scopeId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString, "scope-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }
}

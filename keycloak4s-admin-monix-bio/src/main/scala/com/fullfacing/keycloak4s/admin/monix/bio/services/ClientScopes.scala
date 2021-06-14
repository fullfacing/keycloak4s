package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{ClientScope, KeycloakError, Mappings, Role}
import monix.bio.IO

import scala.collection.immutable.Seq

class ClientScopes(implicit client: KeycloakClient) {

  /** Creates a new client scope. Client Scope’s name must be unique. */
  def create(clientScope: ClientScope.Create): IO[KeycloakError, UUID] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.post[Headers](path, clientScope).map(extractUuid).flatMap(IO.fromEither)
  }

  /** Retrieves client scopes. */
  def fetch(): IO[KeycloakError, List[ClientScope]] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.get[List[ClientScope]](path)
  }

  /** Retrieves a client scope by id. */
  def fetchById(scopeId: UUID): IO[KeycloakError, ClientScope] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.get[ClientScope](path)
  }

  /** Updates a client scope. */
  def update(scopeId: UUID, clientScope: ClientScope.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.put[Unit](path, clientScope)
  }

  /** Deletes a client scope. */
  def delete(scopeId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.delete[Unit](path)
  }

  /** Retrieves a list of scope mappings for a client scope. */
  def fetchMappedRoles(scopeId: UUID): IO[KeycloakError, Mappings] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings")
    client.get[Mappings](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------- Client Level Role Mappings ------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  
  /** Adds client-level roles to a client scope. */
  def addClientRoles(scopeId: UUID, clientId: UUID, roleNames: List[String]): IO[KeycloakError, Unit] = {
    val body = roleNames.map(Role.Name)
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "clients", clientId)
    client.post[Unit](path, body)
  }

  /** Retrieves a list of client-level roles mapped to a client scope. */
  def fetchMappedClientRoles(scopeId: UUID, clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "clients", clientId)
    client.get[List[Role]](path)
  }

  /** Removes client-level roles from a client scope. */
  def removeClientRoles(scopeId: UUID, clientId: UUID, roleNames: List[String]): IO[KeycloakError, Unit] = {
    val body = roleNames.map(Role.Name)
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "clients", clientId)
    client.delete[Unit](path, body)
  }

  /** Retrieves a list of available client-level roles for a client scope. */
  def fetchAvailableClientRoles(scopeId: UUID, clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "clients", clientId, "available")
    client.get[List[Role]](path)
  }

  /** Retrieves a list of effective client-level roles of a client scope. */
  def fetchEffectiveClientRoles(scopeId: UUID, clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "clients", clientId, "composite")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------- Realm Level Role mappings -------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  
  /** Maps a set of realm-level roles to the client scope. */
  def addRealmRoles(scopeId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "realm")
    client.post[Unit](path, body)
  }

  /** Retrieves a list of realm-level roles mapped to a client scope. */
  def fetchMappedRealmRoles(scopeId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "realm")
    client.get[List[Role]](path)
  }

  /** Unmaps a set of realm-level roles from a client scope. */
  def removeRealmRoles(scopeId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "realm")
    client.delete[Unit](path, body)
  }

  /** Retrieves a list of available realm-level roles for a client scope. */
  def fetchAvailableRealmRoles(scopeId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /** Retrieves a list of effective realm-level roles of a client scope. */
  def fetchEffectiveRealmRoles(scopeId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId, "scope-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }
}

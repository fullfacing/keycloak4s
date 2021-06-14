package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}

import scala.collection.immutable.Seq

class Groups[R[+_]: Concurrent](implicit client: KeycloakClient[R]) {

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  
  /** Creates a group. */
  def create(group: Group.Create): R[Either[KeycloakError, UUID]] = {
    val path: Path = Seq(client.realm, "groups")
    Concurrent[R].map(client.post[Headers](path, group))(extractUuid)
  }

  /** Creates a sub group within a group. */
  def createSubGroup(groupId: UUID, group: Group.Create): R[Either[KeycloakError, Group]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "children")
    client.post[Group](path, group)
  }

  /** Retrieves a list of groups. */
  def fetch(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, Seq[Group]]] = {
    val query = createQuery(("first", first), ("max", max), ("search", search))
    val path: Path = Seq(client.realm, "groups")
    client.get[Seq[Group]](path, query = query)
  }

  /** Retrieves a group by id. */
  def fetchById(groupId: UUID): R[Either[KeycloakError, Group]] = {
    val path: Path = Seq(client.realm, "groups", groupId)
    client.get[Group](path)
  }

  /** Composite of create and fetch. */
  def createAndRetrieve(group: Group.Create): R[Either[KeycloakError, Group]] =
    Concurrent[R].flatMap(create(group)) {
      case Right(id) => fetchById(id)
      case Left(err) => Concurrent[R].pure(err.asLeft)
    }

  /** Updates a group. */
  def update(groupId: UUID, group: Group.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId)
    client.put[Unit](path, group)
  }

  /** Deletes a group. */
  def delete(groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId)
    client.delete[Unit](path)
  }

  /** Retrieves the amount of groups. */
  def count(search: Option[String] = None, top: Boolean = false): R[Either[KeycloakError, Count]] = {
    val query = createQuery(("search", search), ("top", Some(top)))
    val path: Path = Seq(client.realm, "groups", "count")
    client.get[Count](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Users ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------- //
  
  /** Retrieves the users of a group. */
  def fetchUsers(groupId: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[User]]] = {
    val query = createQuery(("first", first), ("max", max))
    val path: Path = Seq(client.realm, "groups", groupId, "members")
    client.get[List[User]](path, query = query)
  }
  
  /** Adds a user to a group. */
  def addUserToGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "users", userId, "groups", groupId)
    client.put[Unit](path)
  }

  /** Removes a user from a group. */
  def removeUserFromGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "users", userId, "groups", groupId)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Roles ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------- //
  
  /** Retrieves the roles of a group. */
  def fetchRoles(groupId: UUID): R[Either[KeycloakError, Mappings]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings")
    client.get[Mappings](path)
  }

  /** Retrieves the realm roles of a group. */
  def fetchRealmRoles(groupId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "realm")
    client.get[List[Role]](path)
  }

  /** Retrieves the available realm roles for a group. */
  def fetchAvailableRealmRoles(groupId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /** Retrieves the effective realm roles of a group. */
  def fetchEffectiveRealmRoles(groupId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }

  /** Adds a realm role to a group. */
  def addRealmRoles(groupId: UUID, roles: List[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "realm")
    client.post[Unit](path, roles)
  }

  /** Removes a realm role from a group. */
  def removeRealmRoles(groupId: UUID, roles: List[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "realm")
    client.delete[Unit](path, roles)
  }

  // --- Client Level Roles --- //

  /** Adds a client role to a group. */
  def addClientRoles(clientId: UUID, groupId: UUID, roles: List[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "clients", clientId)
    client.post[Unit](path, roles)
  }

  /** Retrieves the client roles of a group. */
  def fetchClientRoles(clientId: UUID, groupId: UUID): R[Either[KeycloakError, Seq[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "clients", clientId)
    client.get[Seq[Role]](path)
  }

  /** Removes a client role from a group. */
  def removeClientRoles(clientId: UUID, groupId: UUID, roles: Seq[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "clients", clientId)
    client.delete[Unit](path, roles)
  }

  /** Retrieves the available client roles for a group. */
  def fetchAvailableClientRoles(clientId: UUID, groupId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "clients", clientId, "available")
    client.get[List[Role]](path)
  }

  /** Retrieves the effective client roles of a group. */
  def fetchEffectiveClientRoles(clientId: UUID, groupId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "role-mappings", "clients", clientId, "composite")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Permissions ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------------- //

  /** Retrieves details of a group's management permissions. */
  def fetchManagementPermissions(groupId: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Enables a group's management permissions. */
  def enableManagementPermissions(groupId: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(true))
  }

  /** Disables a group's management permissions. */
  def disableManagementPermissions(groupId: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "groups", groupId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(false))
  }
}

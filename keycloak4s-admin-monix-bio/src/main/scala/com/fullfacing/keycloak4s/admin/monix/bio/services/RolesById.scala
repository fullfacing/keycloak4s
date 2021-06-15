package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, ManagementPermission, Role}
import monix.bio.IO

import scala.collection.immutable.Seq

class RolesById(implicit client: KeycloakClient) {

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  /** Fetch the role with the given role ID. */
  def fetch(roleId: UUID): IO[KeycloakError, Role] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId)
    client.get[Role](path)
  }

  /** Update details of the given role. */
  def update(roleId: UUID, role: Role.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId)
    client.put[Unit](path, role)
  }

  /** Delete the role. */
  def delete(roleId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------ Composite Roles ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /** Make the role a composite role by associating some child roles. */
  def addCompositeRoles(roleId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "composites")
    client.post[Unit](path, body)
  }

  /** Get role’s children Returns a set of role’s children provided the role is a composite. */
  def fetchCompositeRoles(roleId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "composites")
    client.get[List[Role]](path)
  }

  /** Remove a set of roles from the role’s composite. */
  def removeCompositeRoles(roleId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "composites")
    client.delete[Unit](path, body)
  }

  /** Get client-level roles for the client that are in the role’s composite. */
  def fetchClientLevelCompositeRoles(roleId: UUID, clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "composites", "clients", clientId)
    client.get[List[Role]](path)
  }

  /** Get realm-level roles that are in the role’s composite. */
  def fetchRealmLevelCompositeRoles(roleId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "composites", "realm")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Permissions -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /** Retrieve the management permission details of the role. */
  def fetchManagementPermissions(roleId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Enable management permissions for the role. */
  def enableManagementPermissions(roleId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(true))
  }

  /** Disable management permissions for the role. */
  def disableManagementPermissions(roleId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "roles-by-id", roleId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(false))
  }
}
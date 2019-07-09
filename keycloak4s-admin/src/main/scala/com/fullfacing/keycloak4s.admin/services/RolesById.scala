package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{ManagementPermission, Role}
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class RolesById[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val `roles-by-id` = "roles-by-id"

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  def fetch(id: UUID): R[Either[KeycloakError, Role]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id)
    client.get[Role](path)
  }

  def update(id: UUID, role: Role.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id)
    client.put[Unit](path, role)
  }

  def delete(id: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------ Composite Roles ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  /** Make the role a composite role by associating some child roles. */
  def addCompositeRoles(id: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, `roles-by-id`, id, "composites")
    client.post[Unit](path, body)
  }

  /** Get role’s children Returns a set of role’s children provided the role is a composite. */
  def fetchCompositeRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id, "composites")
    client.get[List[Role]](path)
  }

  /** Remove a set of roles from the role’s composite. */
  def removeCompositeRoles(id: UUID, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, `roles-by-id`, id, "composites")
    client.delete[Unit](path, body)
  }

  /** Get client-level roles for the client that are in the role’s composite. */
  def fetchClientLevelCompositeRoles(id: UUID, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id, "composites", "clients", clientId)
    client.get[List[Role]](path)
  }

  /** Get realm-level roles that are in the role’s composite. */
  def fetchRealmLevelCompositeRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id, "composites", "realm")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Permissions -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //
  def authPermissionsInitialised(id: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  def initialiseRoleAuthPermissions(id: UUID, ref: ManagementPermission.Enable): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, `roles-by-id`, id, "management", "permissions")
    client.put[ManagementPermission](path, ref)
  }
}
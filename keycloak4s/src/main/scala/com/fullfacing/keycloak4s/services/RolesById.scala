package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{ManagementPermission, Role}

import scala.collection.immutable.Seq

class RolesById[R[_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  private val resource = "roles-by-id"

  /**
   * id of role
   *
   * @param roleId id of role
   * @return
   */
  def fetch(roleId: String): R[Role] = {
    val path = Seq(keycloakClient.realm, resource, roleId)
    keycloakClient.get[Role](path)
  }

  /**
   * Update the role
   *
   * @param roleId id of role
   * @param role
   * @return
   */
  def update(roleId: String, role: Role): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, roleId)
    keycloakClient.put[Role, Unit](path, role)
  }

  /**
   * Delete the role
   *
   * @param roleId id of role
   * @return
   */
  def delete(roleId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, roleId)
    keycloakClient.delete(path)
  }

  /**
   * Make the role a composite role by associating some child roles
   *
   * @param roleId id of role
   * @param role
   * @return
   */
  def addSubRoles(roleId: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "composites")
    keycloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get role’s children Returns a set of role’s children provided the role is a composite
   *
   * @param roleId id of role
   * @return
   */
  def fetchSubRoles(roleId: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "composites")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove a set of roles from the role’s composite
   *
   * @param roleId id of role
   * @param roles   A set of roles to be removed
   * @return
   */
  def removeSubRoles(roleId: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "composites")
    keycloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * Get client-level roles for the client that are in the role’s composite
   *
   * @param roleId id of role
   * @param client
   * @return
   */
  def getSubRoleClientLevelRoles(roleId: String, client: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "composites", "clients", client)
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Get realm-level roles that are in the role’s composite
   *
   * @param roleId id of role
   * @return
   */
  def getSubRoleRealmLevelRoles(roleId: String): R[List[Role]] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "composites", "realm")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Return object stating whether role Authorization permissions have been initialized or not and a reference
   *
   * @param roleId id of role
   * @return
   */
  def authPermissionsInitialised(roleId: String): R[ManagementPermission] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "management", "permissions")
    keycloakClient.get[ManagementPermission](path)
  }

  /**
   * TODO confirm description: Initialise role authorization permissions
   *
   * @param roleId id of role
   * @param ref    ..
   * @return
   */
  def initialiseRoleAuthPermissions(roleId: String, ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(keycloakClient.realm, resource, roleId, "management", "permissions")
    keycloakClient.put[ManagementPermission, ManagementPermission](path, ref)
  }
}

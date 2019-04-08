package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{KeycloakError, ManagementPermission, Role}

import scala.collection.immutable.Seq

class RolesById[R[+_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  private val resource = "roles-by-id"

  /**
   * id of role
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @return
   */
  def fetch(realm: String, roleId: String): R[Either[KeycloakError, Role]] = {
    val path = Seq(realm, resource, roleId)
    keycloakClient.get[Role](path)
  }

  /**
   * Update the role
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @param role
   * @return
   */
  def update(realm: String, roleId: String, role: Role): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, resource, roleId)
    keycloakClient.put[Role, Unit](path, role)
  }

  /**
   * Delete the role
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @return
   */
  def delete(realm: String, roleId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, resource, roleId)
    keycloakClient.delete(path)
  }

  /**
   * Make the role a composite role by associating some child roles
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @param role
   * @return
   */
  def addSubRoles(realm: String, roleId: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, resource, roleId, "composites")
    keycloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get role’s children Returns a set of role’s children provided the role is a composite
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @return
   */
  def fetchSubRoles(realm: String, roleId: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, resource, roleId, "composites")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Remove a set of roles from the role’s composite
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @param roles   A set of roles to be removed
   * @return
   */
  def removeSubRoles(realm: String, roleId: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, resource, roleId, "composites")
    keycloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * Get client-level roles for the client that are in the role’s composite
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @param client
   * @return
   */
  def getSubRoleClientLevelRoles(realm: String, roleId: String, client: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, resource, roleId, "composites", "clients", client)
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Get realm-level roles that are in the role’s composite
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @return
   */
  def getSubRoleRealmLevelRoles(realm: String, roleId: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, resource, roleId, "composites", "realm")
    keycloakClient.get[List[Role]](path)
  }

  /**
   * Return object stating whether role Authorization permissions have been initialized or not and a reference
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @return
   */
  def authPermissionsInitialised(realm: String, roleId: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, resource, roleId, "management", "permissions")
    keycloakClient.get[ManagementPermission](path)
  }

  /**
   * TODO confirm description: Initialise role authorization permissions
   *
   * @param realm  realm name (not id!)
   * @param roleId id of role
   * @param ref    ..
   * @return
   */
  def initialiseRoleAuthPermissions(realm: String, roleId: String, ref: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, resource, roleId, "management", "permissions")
    keycloakClient.put[ManagementPermission, ManagementPermission](path, ref)
  }
}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{ManagementPermission, Role, User}

import scala.collection.immutable.Seq

class Roles[R[_]: Concurrent, S](implicit keyCloakClient: KeycloakClient[R, S]) {

  private val clients_path = "clients"
  private val roles_path   = "roles"

  /**
   * Create a new role for the realm or client
   *
   * @param id    id of client (not client-id)
   * @param role
   * @return
   */
  def createClientRole(id: String, role: Role): R[Unit] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path)
    keyCloakClient.post[Role, Unit](path, role)
  }

  /**
   * Get all roles for the realm or client
   *
   * @param id    id of client (not client-id)
   * @return
   */
  def fetch(id: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path)
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Get a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def getByName(id: String, roleName: String): R[Role] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName)
    keyCloakClient.get[Role](path)
  }

  /**
   * Update a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param role
   * @return
   */
  def updateByName(id: String, roleName: String, role: Role): R[Unit] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName)
    keyCloakClient.put[Role, Unit](path, role)
  }

  /**
   * Delete a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def removeByName(id: String, roleName: String): R[Unit] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName)
    keyCloakClient.delete(path)
  }

  /**
   * Add a composite to the role
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param roles
   * @return
   */
  def addComposites(id: String, roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "composites")
    keyCloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get composites of the role
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def fetchRoleComposites(id: String, roleName: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "composites")
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param roles    roles to remove
   * @return
   */
  def removeCompositeRoles(id: String, roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "composites")
    keyCloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param client
   * @return
   */
  def fetchCompositesAppLevelRoles(id: String, roleName: String, client: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "composites", "clients", client)
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def fetchCompositesRealmLevelRoles(id: String, roleName: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "composites", "realm")
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Return object stating whether role Authorisation permissions have been initialized or not and a reference
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def roleAuthPermissionsInitialised(id: String, roleName: String): R[ManagementPermission] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "management", "permissions")
    keyCloakClient.get[ManagementPermission](path)
  }

  /**
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def setAuthPermissions(id: String, roleName: String, ref: ManagementPermission): R[ManagementPermission] = { // TODO Determine functionality
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "management", "permissions")
    keyCloakClient.put[ManagementPermission, ManagementPermission](path, ref)
  }

  /**
   * Return List of Users that have the specified role name
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def usersByRoleName(id: String, roleName: String, first: Option[Int], max: Option[Int]): R[List[User]] = {
    val path = Seq(keyCloakClient.realm, clients_path, id, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    keyCloakClient.get[List[User]](path, query = query)
  }

  /**
   * Create a new role for the realm or client
   *
   * @param role
   * @return
   */
  def createRealmRole(role: Role): R[Unit] = {
    val path = Seq(keyCloakClient.realm, roles_path)
    keyCloakClient.post[Role, Unit](path, role)
  }

  /**
   * Get all roles for the realm or client
   */
  def fetchRealmRoles(): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, roles_path)
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Get a role by name
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleByRoleName(roleName: String): R[Role] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName)
    keyCloakClient.get[Role](path)
  }

  /**
   * Update a role by name
   *
   * @param roleName role’s name (not id!)
   * @param role     Updated role
   * @return
   */
  def updateRealmRoleByName(roleName: String, role: Role): R[Unit] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName)
    keyCloakClient.put[Role, Unit](path, role)
  }

  /**
   * Delete a role by name
   *
   * @param roleName role’s name (not id!)
   * @param role     Role to be deleted
   * @return
   */
  def deleteRealmRoleByName(roleName: String, role: String): R[Unit] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName)
    keyCloakClient.delete[String, Unit](path, role)
  }

  /**
   * Add a composite to the role
   *
   * @param roleName role’s name (not id!)
   * @param roles    Composite roles to be added
   * @return
   */
  def addCompositeToRealmRole(roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "composites")
    keyCloakClient.post[List[Role], Unit](path, roles)
  }

  /**
   * Get composites of the role
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleComposites(roleName: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "composites")
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param roleName role’s name (not id!)
   * @param roles    roles to be removed
   * @return
   */
  def removeRolesFromRolesComposite(roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "composites")
    keyCloakClient.delete[List[Role], Unit](path, roles)
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param roleName role’s name (not id!)
   * @param client
   * @return
   */
  def fetchRolesCompositesAppLevelRoles(roleName: String, client: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "composites", "clients", client)
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def fetchRolesCompositeRealmLevelRoles(roleName: String): R[List[Role]] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "composites", "realm")
    keyCloakClient.get[List[Role]](path)
  }

  /**
   * Return object stating whether role Authoirzation permissions have been initialized or not and a reference
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def realmRoleAuthInitialised(roleName: String): R[ManagementPermission] = {
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "management", "permissions")
    keyCloakClient.get[ManagementPermission](path)
  }

  /**
   * To be determined
   *
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def TBD(roleName: String, ref: ManagementPermission): R[ManagementPermission] = { //TODO determine functionality
    val path = Seq(keyCloakClient.realm, roles_path, roleName, "management", "permissions")
    keyCloakClient.put[ManagementPermission, ManagementPermission](path, ref)
  }

  /**
   * Return List of Users that have the specified role name
   *
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def fetchUsersByRoleName(roleName: String, first: Option[Int], max: Option[Int]): R[User] = {
    val path  = Seq(keyCloakClient.realm, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    keyCloakClient.get[User](path, query = query)
  }
}

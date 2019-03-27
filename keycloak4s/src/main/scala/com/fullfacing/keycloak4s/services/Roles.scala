package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{ManagementPermission, Role, User}

import scala.collection.immutable.Seq

class Roles[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val clients_path  = "clients"
  private val roles_path    = "roles"
  private val `roles-by-id` = "roles-by-id"

  ///////////////////////////////////////////////////////////////////////////
  // Get Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Get all roles for the client
   *
   * @param id    id of client (not client-id)
   * @return
   */
  def getClientRoles(id: String): R[List[Role]] = {
    val path = Seq(client.realm, clients_path, id, roles_path)
    client.get[List[Role]](path)
  }

  /**
   * Get all roles for the realm
   */
  def getRealmRoles(): R[List[Role]] = {
    val path = Seq(client.realm, roles_path)
    client.get[List[Role]](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Role
  ///////////////////////////////////////////////////////////////////////////

  /**
   * @param roleId id of role
   * @return
   */
  def getById(roleId: String): R[Role] = {
    val path = Seq(client.realm, `roles-by-id`, roleId)
    client.get[Role](path)
  }

  /**
   * Get a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def getClientRoleByName(id: String, roleName: String): R[Role] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName)
    client.get[Role](path)
  }

  /**
   * Get a role by name
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleByName(roleName: String): R[Role] = {
    val path = Seq(client.realm, roles_path, roleName)
    client.get[Role](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Create Role
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Create a new role for the client
   *
   * @param id    id of client (not client-id)
   * @param role
   * @return
   */
  def createClientRole(id: String, role: Role): R[Unit] = {
    val path = Seq(client.realm, clients_path, id, roles_path)
    client.post[Role, Unit](path, role)
  }

  /**
   * Create a new role for the realm
   *
   * @param role
   * @return
   */
  def createRealmRole(role: Role): R[Unit] = {
    val path = Seq(client.realm, roles_path)
    client.post[Role, Unit](path, role)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Update Role
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Update the role
   *
   * @param roleId id of role
   * @param role
   * @return
   */
  def updateById(roleId: String, role: Role): R[Unit] = {
    val path = Seq(client.realm, `roles-by-id`, roleId)
    client.put[Role, Unit](path, role)
  }

  /**
   * Update a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param role
   * @return
   */
  def updateClientRoleByName(id: String, roleName: String, role: Role): R[Unit] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName)
    client.put[Role, Unit](path, role)
  }

  /**
   * Update a role by name
   *
   * @param roleName role’s name (not id!)
   * @param role     Updated role
   * @return
   */
  def updateRealmRoleByName(roleName: String, role: Role): R[Unit] = {
    val path = Seq(client.realm, roles_path, roleName)
    client.put[Role, Unit](path, role)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Delete Role
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Delete the role
   *
   * @param roleId id of role
   * @return
   */
  def removeById(roleId: String): R[Unit] = {
    val path = Seq(client.realm, `roles-by-id`, roleId)
    client.delete(path)
  }

  /**
   * Delete a role by name
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def removeClientRoleByName(id: String, roleName: String): R[Unit] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName)
    client.delete(path)
  }

  /**
   * Delete a role by name
   *
   * @param roleName role’s name (not id!)
   * @param role     Role to be deleted
   * @return
   */
  def removeRealmRoleByName(roleName: String, role: String): R[Unit] = {
    val path = Seq(client.realm, roles_path, roleName)
    client.delete[String, Unit](path, role)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Composite Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Get role’s children Returns a set of role’s children provided the role is a composite
   *
   * @param roleId id of role
   * @return
   */
  def getRoleComposites(roleId: String): R[List[Role]] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "composites")
    client.get[List[Role]](path)
  }

  /**
   * Get composites of the role
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def getClientRoleComposites(id: String, roleName: String): R[List[Role]] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "composites")
    client.get[List[Role]](path)
  }

  /**
   * Get composites of the role
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleComposites(roleName: String): R[List[Role]] = {
    val path = Seq(client.realm, roles_path, roleName, "composites")
    client.get[List[Role]](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Add Composite Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Make the role a composite role by associating some child roles
   *
   * @param roleId id of role
   * @param roles
   * @return
   */
  def addRoleComposites(roleId: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "composites")
    client.post[List[Role], Unit](path, roles)
  }

  /**
   * Add a composite to the role
   *
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param roles
   * @return
   */
  def addClientRoleComposites(id: String, roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "composites")
    client.post[List[Role], Unit](path, roles)
  }

  /**
   * Add a composite to the role
   *
   * @param roleName role’s name (not id!)
   * @param roles    Composite roles to be added
   * @return
   */
  def addRealmRoleComposites(roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, roles_path, roleName, "composites")
    client.post[List[Role], Unit](path, roles)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Remove Composite Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Remove a set of roles from the role’s composite
   *
   * @param roleId id of role
   * @param roles   A set of roles to be removed
   * @return
   */
  def removeRoleComposites(roleId: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "composites")
    client.delete[List[Role], Unit](path, roles)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param roles    roles to remove
   * @return
   */
  def removeClientRoleComposites(id: String, roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "composites")
    client.delete[List[Role], Unit](path, roles)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param roleName role’s name (not id!)
   * @param roles    roles to be removed
   * @return
   */
  def removeRealmRoleComposites(roleName: String, roles: List[Role]): R[Unit] = {
    val path = Seq(client.realm, roles_path, roleName, "composites")
    client.delete[List[Role], Unit](path, roles)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Client Composite Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Get client-level roles for the client that are in the role’s composite
   *
   * @param roleId             id of role
   * @param compositeClientId  id of client of composite role
   * @return
   */
  def getSubRoleClientLevelRoles(roleId: String, compositeClientId: String): R[List[Role]] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "composites", "clients", compositeClientId)
    client.get[List[Role]](path)
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param id                 id of client (not client-id)
   * @param roleName           role’s name (not id!)
   * @param compositeClientId  id of client of composite role
   * @return
   */
  def getClientLevelCompositeClientRoles(id: String, roleName: String, compositeClientId: String): R[List[Role]] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "composites", "clients", compositeClientId)
    client.get[List[Role]](path)
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param roleName           role’s name (not id!)
   * @param compositeClientId  id of client of composite role
   * @return
   */
  def getRealmLevelCompositeClientRoles(roleName: String, compositeClientId: String): R[List[Role]] = {
    val path = Seq(client.realm, roles_path, roleName, "composites", "clients", compositeClientId)
    client.get[List[Role]](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Realm Composite Roles
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Get realm-level roles that are in the role’s composite
   *
   * @param roleId id of role
   * @return
   */
  def getCompositeRealmRoles(roleId: String): R[List[Role]] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "composites", "realm")
    client.get[List[Role]](path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def getClientLevelCompositeRealmRoles(id: String, roleName: String): R[List[Role]] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "composites", "realm")
    client.get[List[Role]](path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmLevelCompositeRealmRoles(roleName: String): R[List[Role]] = {
    val path = Seq(client.realm, roles_path, roleName, "composites", "realm")
    client.get[List[Role]](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Role Users
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Return List of Users that have the specified role name
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def getClientRoleUsers(id: String, roleName: String, first: Option[Int], max: Option[Int]): R[List[User]] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    client.get[List[User]](path, query = query)
  }

  /**
   * Return List of Users that have the specified role name
   *
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def getRealmRoleUsers(roleName: String, first: Option[Int], max: Option[Int]): R[User] = {
    val path  = Seq(client.realm, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    client.get[User](path, query = query)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Get Role Permissions
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Return object stating whether role Authorization permissions have been initialized or not and a reference
   *
   * @param roleId id of role
   * @return
   */
  def getRolePermissions(roleId: String): R[ManagementPermission] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Return object stating whether role Authorisation permissions have been initialized or not and a reference
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def getClientRolePermissions(id: String, roleName: String): R[ManagementPermission] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Return object stating whether role Authorisation permissions have been initialized or not and a reference
   *
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRolePermissions(roleName: String): R[ManagementPermission] = {
    val path = Seq(client.realm, roles_path, roleName, "management", "permissions")
    client.get[ManagementPermission](path)
  }


  ///////////////////////////////////////////////////////////////////////////
  // Set Role Permissions
  ///////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param roleId id of role
   * @param ref    ..
   * @return
   */
  def setRolePermissions(roleId: String, ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(client.realm, `roles-by-id`, roleId, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](path, ref)
  }

  /**
   *
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def setClientRolePermissions(id: String, roleName: String, ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(client.realm, clients_path, id, roles_path, roleName, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](path, ref)
  }

  /**
   *
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def setRealmRolePermissions(roleName: String, ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(client.realm, roles_path, roleName, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](path, ref)
  }
}

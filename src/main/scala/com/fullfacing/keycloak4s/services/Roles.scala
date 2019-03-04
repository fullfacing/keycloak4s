package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.SttpClient.UnknownResponse
import com.fullfacing.keycloak4s.models.{ManagementPermission, Role, User}
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object Roles {

  private val clients_path = "clients"
  private val roles_path   = "roles"

  /**
   * Create a new role for the realm or client
   *
   * @param realm realm name (not id!)
   * @param id    id of client (not client-id)
   * @param role
   * @return
   */
  def createClientRole(realm: String, id: String, role: Role): AsyncApolloResponse[UnknownResponse] = {
    val path = Seq(realm, clients_path, id, roles_path)
    SttpClient.post(role, path)
  }

  /**
   * Get all roles for the realm or client
   *
   * @param realm realm name (not id!)
   * @param id    id of client (not client-id)
   * @return
   */
  def fetch(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, clients_path, id, roles_path)
    SttpClient.get(path)
  }

  /**
   * Get a role by name
   *
   * @param realm     realm name (not id!)
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def getByName(realm: String, id: String, roleName: String): AsyncApolloResponse[Role] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName)
    SttpClient.get(path)
  }

  /**
   * Update a role by name
   *
   * @param realm     realm name (not id!)
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param role
   * @return
   */
  def updateByName(realm: String, id: String, roleName: String, role: Role): AsyncApolloResponse[UnknownResponse] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName)
    SttpClient.put(role, path)
  }

  /**
   * Delete a role by name
   *
   * @param realm     realm name (not id!)
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def removeByName(realm: String, id: String, roleName: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName)
    SttpClient.delete(path)
  }

  /**
   * Add a composite to the role
   *
   * @param realm     realm name (not id!)
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @param roles
   * @return
   */
  def addComposites(realm: String, id: String, roleName: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "composites")
    SttpClient.post(roles, path)
  }

  /**
   * Get composites of the role
   *
   * @param realm     realm name (not id!)
   * @param id        id of client (not client-id)
   * @param roleName  role’s name (not id!)
   * @return
   */
  def fetchRoleComposites(realm: String, id: String, roleName: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "composites")
    SttpClient.get(path)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param roles    roles to remove
   * @return
   */
  def removeCompositeRoles(realm: String, id: String, roleName: String, roles: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "composites")
    SttpClient.delete(roles, path, Seq.empty[KeyValue])
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param client
   * @return
   */
  def fetchCompositesAppLevelRoles(realm: String, id: String, roleName: String, client: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "composites", "clients", client)
    SttpClient.get(path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def fetchCompositesRealmLevelRoles(realm: String, id: String, roleName: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "composites", "realm")
    SttpClient.get(path)
  }

  /**
   * Return object stating whether role Authorisation permissions have been initialized or not and a reference
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @return
   */
  def roleAuthPermissionsInitialised(realm: String, id: String, roleName: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "management", "permissions")
    SttpClient.get(path)
  }

  /**
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def setAuthPermissions(realm: String, id: String, roleName: String, ref: ManagementPermission): AsyncApolloResponse[ManagementPermission] = { // TODO Determine functionality
    val path = Seq(realm, clients_path, id, roles_path, roleName, "management", "permissions")
    SttpClient.put(ref, path)
  }

  /**
   * Return List of Users that have the specified role name
   *
   * @param realm    realm name (not id!)
   * @param id       id of client (not client-id)
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def usersByRoleName(realm: String, id: String, roleName: String, first: Option[Int], max: Option[Int]): AsyncApolloResponse[List[User]] = {
    val path = Seq(realm, clients_path, id, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    SttpClient.get(path, query)
  }

  /**
   * Create a new role for the realm or client
   *
   * @param realm Realm name
   * @param role
   * @return
   */
  def createRealmRole(realm: String, role: Role): AsyncApolloResponse[UnknownResponse] = {
    val path = Seq(realm, roles_path)
    SttpClient.post(role, path)
  }

  /**
   * Get all roles for the realm or client
   *
   * @param realm Realm name
   * @return
   */
  def fetchRealmRoles(realm: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, roles_path)
    SttpClient.get(path)
  }

  /**
   * Get a role by name
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleByRoleName(realm: String, roleName: String): AsyncApolloResponse[Role] = {
    val path = Seq(realm, roles_path, roleName)
    SttpClient.get(path)
  }

  /**
   * Update a role by name
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param role     Updated role
   * @return
   */
  def updateRealmRoleByName(realm: String, roleName: String, role: Role): AsyncApolloResponse[UnknownResponse] = {
    val path = Seq(realm, roles_path, roleName)
    SttpClient.put(role, path)
  }

  /**
   * Delete a role by name
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param role     Role to be deleted
   * @return
   */
  def deleteRealmRoleByName(realm: String, roleName: String, role: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, roles_path, roleName)
    SttpClient.delete(role, path, Seq.empty[KeyValue])
  }

  /**
   * Add a composite to the role
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param roles    Composite roles to be added
   * @return
   */
  def addCompositeToRealmRole(realm: String, roleName: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, roles_path, roleName, "composites")
    SttpClient.post(roles, path)
  }

  /**
   * Get composites of the role
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @return
   */
  def getRealmRoleComposites(realm: String, roleName: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, roles_path, roleName, "composites")
    SttpClient.get(path)
  }

  /**
   * Remove roles from the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param roles    roles to be removed
   * @return
   */
  def removeRolesFromRolesComposite(realm: String, roleName: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, roles_path, roleName, "composites")
    SttpClient.delete(roles, path, Seq.empty[KeyValue])
  }

  /**
   * An app-level roles for the specified app for the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param client
   * @return
   */
  def fetchRolesCompositesAppLevelRoles(realm: String, roleName: String, client: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, roles_path, roleName, "composites", "clients", client)
    SttpClient.get(path)
  }

  /**
   * Get realm-level roles of the role’s composite
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @return
   */
  def fetchRolesCompositeRealmLevelRoles(realm: String, roleName: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, roles_path, roleName, "composites", "realm")
    SttpClient.get(path)
  }

  /**
   * Return object stating whether role Authoirzation permissions have been initialized or not and a reference
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @return
   */
  def realmRoleAuthInitialised(realm: String, roleName: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, roles_path, roleName, "management", "permissions")
    SttpClient.get(path)
  }

  /**
   * To be determined
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param ref
   * @return
   */
  def TBD(realm: String, roleName: String, ref: ManagementPermission): AsyncApolloResponse[ManagementPermission] = { //TODO determine functionality
    val path = Seq(realm, roles_path, roleName, "management", "permissions")
    SttpClient.put(ref, path)
  }

  /**
   * Return List of Users that have the specified role name
   *
   * @param realm    realm name (not id!)
   * @param roleName role’s name (not id!)
   * @param first
   * @param max
   * @return
   */
  def fetchUsersByRoleName(realm: String, roleName: String, first: Option[Int], max: Option[Int]): AsyncApolloResponse[User] = {
    val path  = Seq(realm, roles_path, roleName, "users")
    val query = createQuery(("first", first), ("max", max))
    SttpClient.get(path, query)
  }
}

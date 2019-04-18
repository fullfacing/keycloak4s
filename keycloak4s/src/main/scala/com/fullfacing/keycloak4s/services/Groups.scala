package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Groups[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) {

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  def create(group: Group.Create): R[Either[KeycloakError, Group]] = {
    val path = Seq(realm, "groups")
    client.post[Group](path, group)
  }

  def createSubGroup(groupId: UUID, group: Group.Create): R[Either[KeycloakError, Group]] = {
    val path = Seq(realm, "groups", groupId.toString, "children")
    client.post[Group](path, group)
  }

  def fetch(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, Seq[Group]]] = {
    val query = createQuery(("first", first), ("max", max), ("search", search))
    val path = Seq(realm, "groups")
    client.get[Seq[Group]](path, query = query)
  }

  def fetchById(groupId: UUID): R[Either[KeycloakError, Group]] = {
    val path = Seq(realm, "groups", groupId.toString)
    client.get[Group](path)
  }

  def update(groupId: UUID, group: Group): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "groups", groupId.toString)
    client.put[Unit](path, group)
  }

  def delete(groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "groups", groupId.toString)
    client.delete[Unit](path)
  }

  def count(search: Option[String] = None, top: Boolean = false): R[Either[KeycloakError, Count]] = {
    val query = createQuery(("search", search), ("top", Some(top)))
    val path = Seq(realm, "groups", "count")
    client.get[Count](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Users ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------- //
  def fetchUsers(groupId: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[User]]] = {
    val query = createQuery(("first", first), ("max", max))
    val path = Seq(realm, "groups", groupId.toString, "members")
    client.get[List[User]](path, query = query)
  }
  
  def addUserToGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId.toString, "groups", groupId.toString)
    client.put[Unit](path)
  }

  def removeUserFromGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId.toString, "groups", groupId.toString)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Roles ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------- //
  def fetchRoles(id: UUID): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings")
    client.get[Mappings](path)
  }

  def fetchRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings", "realm")
    client.get[List[Role]](path)
  }

  def fetchAvailableRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  def fetchEffectiveRealmRoles(id: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }

  def addRealmRoles(id: UUID, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings", "realm")
    client.post[Unit](path, roles)
  }

  def removeRealmRoles(id: UUID, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "groups", id.toString, "role-mappings", "realm")
    client.delete[Unit](path, roles)
  }

  // ------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Permissions ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------------- //
  def getManagementPermissions(groupId: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "groups", groupId.toString, "management", "permissions")
    client.get[ManagementPermission](path)
  }
  
  def updateManagementPermissions(groupId: UUID, permissions: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "groups", groupId.toString, "management", "permissions")
    client.put[ManagementPermission](path, permissions)
  }
}

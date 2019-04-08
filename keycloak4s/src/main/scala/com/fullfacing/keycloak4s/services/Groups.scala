package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Groups[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * This will update the group and set the parent if it exists. Create it and set the parent if the group doesn’t exist.
   *
   * @param group Object representing the group details.
   * @return
   */
  def addGroupSet(group: Group): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "groups")
    client.post[Group, Unit](path, group)
  }

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getGroups(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, Seq[Group]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max),
      ("search", search)
    )

    val path = Seq(client.realm, "groups")
    client.get[Seq[Group]](path, query = query)
  }

  /**
   * Returns the groups counts.
   *
   * @param search
   * @param top
   * @return
   */
  def getGroupsCounts(search: Option[String] = None, top: Boolean = false): R[Either[KeycloakError, Count]] = {
    val query = createQuery(
      ("search", search),
      ("top", Some(top))
    )

    val path = Seq(client.realm, "groups", "count")
    client.get[Count](path, query = query)
  }

  /**
   * Retrieves a group.
   *
   * @param groupId ID of the group.
   * @return
   */
  def getGroup(groupId: String): R[Either[KeycloakError, Group]] = {
    val path = Seq(client.realm, "groups", groupId)
    client.get[Group](path)
  }

  /**
   * Update group, ignores subgroups.
   *
   * @param groupId ID of the group.
   * @param group   Object representing the group details.
   * @return
   */
  def updateGroup(groupId: String, group: Group): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "groups", groupId)
    client.put[Group, Unit](path, group)
  }

  /**
   * Deletes a group.
   *
   * @param groupId ID of the group.
   * @return
   */
  def deleteGroup(groupId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "groups", groupId)
    client.delete(path)
  }

  /**
   * Set or create child.
   * This will just set the parent if it exists. Create it and set the parent if the group doesn’t exist.
   *
   * @param groupId ID of the group.
   * @param group   Object representing the group details.
   * @return
   */
  def setChild(groupId: String, group: Group): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "groups", groupId, "children")
    client.post[Group, Unit](path, group)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param groupId ID of the group.
   * @return
   */
  def getManagementPermissions(groupId: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "groups", groupId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Update group management permissions .
   *
   * @param groupId     ID of the group.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(groupId: String, permissions: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "groups", groupId, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](path, permissions)
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first
   * @param max
   * @return
   */
  def getUsers(groupId: String, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, Seq[User]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(client.realm, "groups", groupId, "members")
    client.get[Seq[User]](path, query = query)
  }
}

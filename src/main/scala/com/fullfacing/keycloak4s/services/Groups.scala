package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object Groups {

  /**
   * This will update the group and set the parent if it exists. Create it and set the parent if the group doesn’t exist.
   *
   * @param realm Name of the Realm.
   * @param group Object representing the group details.
   * @return
   */
  def addGroupSet(realm: String, group: Group): AsyncApolloResponse[Any] = { //TODO Determine return tyoe.
    val path = Seq(realm, "groups")
    SttpClient.post(group, path)
  }

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param realm   Name of the Realm.
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getGroups(realm: String, first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): AsyncApolloResponse[Seq[Group]] = {
    val query = createQuery(
      ("first", first),
      ("max", max),
      ("search", search)
    )

    val path = Seq(realm, "groups")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Returns the groups counts.
   *
   * @param realm   Name of the Realm.
   * @param search
   * @param top
   * @return
   */
  def getGroupsCounts(realm: String, search: Option[String] = None, top: Boolean = false): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val query = createQuery(
      ("search", search),
      ("top", Some(top))
    )

    val path = Seq(realm, "groups", "count")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Retrieves a group.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def getGroup(groupId: String, realm: String): AsyncApolloResponse[Group] = {
    val path = Seq(realm, "groups", groupId)
    SttpClient.get(path)
  }

  /**
   * Update group, ignores subgroups.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @param group   Object representing the group details.
   * @return
   */
  def getGroup(groupId: String, realm: String, group: Group): AsyncApolloResponse[Group] = {
    val path = Seq(realm, "groups", groupId)
    SttpClient.put(group, path)
  }

  /**
   * Deletes a group.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def deleteGroup(groupId: String, realm: String): AsyncApolloResponse[Group] = {
    val path = Seq(realm, "groups", groupId)
    SttpClient.delete(path)
  }

  /**
   * Update group, ignores subgroups.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @param group   Object representing the group details.
   * @return
   */
  def setChild(groupId: String, realm: String, group: Group): AsyncApolloResponse[Group] = {
    val path = Seq(realm, "groups", groupId, "children")
    SttpClient.post(group, path)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def getManagementPermissions(groupId: String, realm: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "groups", groupId, "management", "permissions")
    SttpClient.get(path)
  }

  /**
   * Update group management permissions .
   *
   * @param groupId     ID of the group.
   * @param realm       Name of the Realm.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(groupId: String, realm: String, permissions: ManagementPermission): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "groups", groupId, "management", "permissions")
    SttpClient.put(permissions, path)
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param realm   Name of the Realm.
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getUsers(groupId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None): AsyncApolloResponse[Seq[Group]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(realm, "groups", groupId, "members")
    SttpClient.get(path, query.to[Seq])
  }
}

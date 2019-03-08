package com.fullfacing.keycloak4s.services

import cats.effect.Effect
import com.fullfacing.apollo.core.Predef.R
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

class Groups[R[_]: Effect, S](implicit client: KeycloakClient[R, S]) {

  /**
   * This will update the group and set the parent if it exists. Create it and set the parent if the group doesn’t exist.
   *
   * @param realm Name of the Realm.
   * @param group Object representing the group details.
   * @return
   */
  def addGroupSet(realm: String, group: Group): R[Any] = { //TODO Determine return type.
    val path = Seq(realm, "groups")
    client.post[Group, Any](group, path)
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
  def getGroups(realm: String, first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Seq[Group]] = {
    val query = createQuery(
      ("first", first),
      ("max", max),
      ("search", search)
    )

    val path = Seq(realm, "groups")
    client.get[Seq[Group]](path, query)
  }

  /**
   * Returns the groups counts.
   *
   * @param realm   Name of the Realm.
   * @param search
   * @param top
   * @return
   */
  def getGroupsCounts(realm: String, search: Option[String] = None, top: Boolean = false): R[Map[String, Any]] = { //TODO Determine return type.
    val query = createQuery(
      ("search", search),
      ("top", Some(top))
    )

    val path = Seq(realm, "groups", "count")
    client.get[Map[String, Any]](path, query)
  }

  /**
   * Retrieves a group.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def getGroup(groupId: String, realm: String): R[Group] = {
    val path = Seq(realm, "groups", groupId)
    client.get[Group](path)
  }

  /**
   * Update group, ignores subgroups.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @param group   Object representing the group details.
   * @return
   */
  def getGroup(groupId: String, realm: String, group: Group): R[Group] = {
    val path = Seq(realm, "groups", groupId)
    client.put[Group, Group](group, path, Seq.empty[KeyValue])
  }

  /**
   * Deletes a group.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def deleteGroup(groupId: String, realm: String): R[Group] = {
    val path = Seq(realm, "groups", groupId)
    client.delete[Group](path, Seq.empty[KeyValue])
  } //TODO FIX DELETE

  /**
   * Update group, ignores subgroups.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @param group   Object representing the group details.
   * @return
   */
  def setChild(groupId: String, realm: String, group: Group): R[Group] = {
    val path = Seq(realm, "groups", groupId, "children")
    client.post[Group, Group](group, path)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param groupId ID of the group.
   * @param realm   Name of the Realm.
   * @return
   */
  def getManagementPermissions(groupId: String, realm: String): R[ManagementPermission] = {
    val path = Seq(realm, "groups", groupId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Update group management permissions .
   *
   * @param groupId     ID of the group.
   * @param realm       Name of the Realm.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(groupId: String, realm: String, permissions: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(realm, "groups", groupId, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](permissions, path, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param realm   Name of the Realm.
   * @param first
   * @param max
   * @return
   */
  def getUsers(groupId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None): R[Seq[Group]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(realm, "groups", groupId, "members")
    client.get[Seq[Group]](path, query)
  }
}

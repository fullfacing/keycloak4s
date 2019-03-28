package com.fullfacing.keycloak4s.monix.services

import com.fullfacing.keycloak4s.models.{Group, User}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.services.createQuery
import monix.eval.Task

import scala.collection.immutable.Seq

class Groups(implicit client: KeycloakClient) {

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getGroups(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): Task[Seq[Group]] = {
    val query = createQuery(
      ("first", first),
      ("max", max),
      ("search", search)
    )

    val path = Seq(client.realm, "groups")
    client.get[Seq[Group]](path, query = query)
  }


  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first
   * @param max
   * @return
   */
  def getUsers(groupId: String, first: Option[Int] = None, max: Option[Int] = None): Task[Seq[User]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(client.realm, "groups", groupId, "members")
    client.get[Seq[User]](path, query = query)
  }
}

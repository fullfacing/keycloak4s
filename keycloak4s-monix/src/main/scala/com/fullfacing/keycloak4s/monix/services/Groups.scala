package com.fullfacing.keycloak4s.monix.services

import com.fullfacing.keycloak4s.models.{Group, User}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import monix.eval.Task
import monix.reactive.Observable

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
  def getGroups(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): Observable[Group] = {
    val query = createQuery(
      ("max", max),
      ("search", search)
    )

    val path = Seq(client.realm, "groups")

    client.getList[Group](path, query, first.getOrElse(0))
  }

  def getGroupsL(first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): Task[List[Group]] = {
    getGroups(first, max, search).consumeWith(consumer())
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first
   * @param max
   * @return
   */
  def getUsers(groupId: String, first: Option[Int] = None, max: Option[Int] = None): Observable[User] = {
    val query = createQuery(
      ("max", max)
    )

    val path = Seq(client.realm, "groups", groupId, "members")
    client.getList[User](path, query, first.getOrElse(0))
  }

  def getUsersL(groupId: String, first: Option[Int] = None, max: Option[Int] = None): Task[List[User]] = {
    getUsers(groupId, first, max).consumeWith(consumer())
  }
}

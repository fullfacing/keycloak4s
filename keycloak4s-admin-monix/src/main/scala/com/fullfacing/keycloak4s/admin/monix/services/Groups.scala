package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.core.models.{Group, User}
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Groups(implicit client: KeycloakClient) {

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param first
   * @param search
   * @return
   */
  def fetch(first: Int = 0, search: Option[String] = None): Observable[Group] = {
    val query = createQuery(("search", search))
    val path  = Seq(client.realm, "groups")

    client.getList[Group](path, query, first)
  }

  def fetchL(first: Int = 0, search: Option[String] = None): Task[List[Group]] = {
    fetch(first, search).consumeWith(consumer())
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first
   * @return
   */
  def fetchUsers(groupId: String, first: Int = 0, batch: Int = 100): Observable[User] = {
    val path  = Seq(client.realm, "groups", groupId, "members")

    client.getList[User](path, offset = first, batch = batch)
  }

  def fetchUsersL(groupId: String, first: Int = 0): Task[List[User]] = {
    fetchUsers(groupId, first).consumeWith(consumer())
  }
}

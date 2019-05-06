package com.fullfacing.keycloak4s.monix.services

import java.nio.ByteBuffer

import com.fullfacing.keycloak4s.models.{Group, KeycloakError, User}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.services
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Groups(implicit client: KeycloakClient) extends services.Groups[Task, Observable[ByteBuffer]](client.realm) {

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param first
   * @param search
   * @return
   */
  def fetchB(first: Int = 0,
             limit: Int = Integer.MAX_VALUE,
             search: Option[String] = None,
             batchSize: Int = 100): Observable[Either[KeycloakError, Seq[Group]]] = {

    val query = createQuery(("search", search))
    val path  = Seq(client.realm, "groups")

    client.getList[Group](path, query, first, limit, batchSize)
  }

  def fetchL(first: Int = 0, limit: Int = Integer.MAX_VALUE, search: Option[String] = None): Task[Either[KeycloakError, Seq[Group]]] = {
    fetchB(first, limit, search).consumeWith(consumer())
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first
   * @return
   */
  def fetchUsers(groupId: String, first: Int = 0, limit: Int = Integer.MAX_VALUE, batchSize: Int = 100): Observable[Either[KeycloakError, Seq[User]]] = {
    val path  = Seq(client.realm, "groups", groupId, "members")

    client.getList[User](path, offset = first, limit = limit, batch = batchSize)
  }

  def fetchUsersL(groupId: String, first: Int = 0, limit: Int = Integer.MAX_VALUE): Task[Either[KeycloakError, Seq[User]]] = {
    fetchUsers(groupId, first).consumeWith(consumer())
  }
}

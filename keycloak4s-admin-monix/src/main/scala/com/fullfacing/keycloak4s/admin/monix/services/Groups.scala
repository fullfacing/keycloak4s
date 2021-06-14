package com.fullfacing.keycloak4s.admin.monix.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.{Group, User}
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Groups[T](implicit client: KeycloakClient[T]) extends services.Groups[Task] {

  /**
   * Retrieves all groups for a Realm. Only name and ids are returned.
   *
   * @param first      Used for pagination, skips the specified number of groups.
   * @param limit      The max amount of groups to return.
   * @param batchSize  The amount of groups each call should return.
   * @param search
   * @return
   */
  def fetchS(first: Int = 0,
             limit: Int = Integer.MAX_VALUE,
             search: Option[String] = None,
             batchSize: Int = 100): Observable[Group] = {

    val query = createQuery(("search", search))
    val path  = Seq(client.realm, "groups")

    client.getList[Group](path, query, first, limit, batchSize)
  }

  def fetchL(first: Int = 0, limit: Int = Integer.MAX_VALUE, search: Option[String] = None): Task[Seq[Group]] = {
    fetchS(first, limit, search).consumeWith(consumer())
  }

  /**
   * Returns a list of users, filtered according to query parameters
   *
   * @param first      Used for pagination, skips the specified number of users.
   * @param limit      The max amount of users to return.
   * @param batchSize  The amount of users each call should return.
   * @return
   */
  def fetchUsersS(groupId: UUID, first: Int = 0, limit: Int = Integer.MAX_VALUE, batchSize: Int = 100): Observable[User] = {
    val path  = Seq(client.realm, "groups", groupId.toString, "members")

    client.getList[User](path, offset = first, limit = limit, batch = batchSize)
  }

  def fetchUsersL(groupId: UUID, first: Int = 0, limit: Int = Integer.MAX_VALUE): Task[Seq[User]] = {
    fetchUsersS(groupId, first, limit).consumeWith(consumer())
  }
}

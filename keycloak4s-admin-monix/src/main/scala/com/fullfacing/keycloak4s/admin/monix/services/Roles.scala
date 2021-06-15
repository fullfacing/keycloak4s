package com.fullfacing.keycloak4s.admin.monix.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.{Group, User}
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Roles[T](implicit client: KeycloakClient[T]) extends services.Roles[Task] {

  /**
   *
   * @param clientId   The UUID of the client.
   * @param name       The name of the role.
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   * @return
   */
  def fetchClientRoleUsersS(clientId: UUID,
                            name: String,
                            first: Int = 0,
                            limit: Int = Integer.MAX_VALUE,
                            batchSize: Int = 100): Observable[User] = {

    val path = Seq(client.realm, "clients", clientId.toString, "roles", name, "users")
    client.getList[User](path, offset = first, limit = limit, batch = batchSize)
  }

  /**
   *
   * @param clientId   The UUID of the client.
   * @param name       The name of the role.
   * @param full       if true, returns a full representation of the Group objects.
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   * @return
   */
  def fetchClientRoleGroupsS(clientId: UUID,
                             name: String,
                             full: Option[Boolean] = None,
                             first: Int = 0,
                             limit: Int = Integer.MAX_VALUE,
                             batchSize: Int = 100): Observable[Group] = {

    val path = Seq(client.realm, "clients", clientId.toString, "roles", name, "groups")
    client.getList[Group](path, createQuery(("full", full)), first, limit, batchSize)
  }

  /**
   *
   * @param name       The name of the role.
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   * @return
   */
  def fetchRealmRoleUsersS(name: String,
                           first: Int = 0,
                           limit: Int = Integer.MAX_VALUE,
                           batchSize: Int = 100): Observable[User] = {

    val path = Seq(client.realm, "roles", name, "users")
    client.getList[User](path, offset = first, limit = limit, batch = batchSize)
  }

  /**
   *
   * @param name       The name of the role.
   * @param full       if true, returns a full representation of the Group objects.
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   * @return
   */
  def fetchRealmRoleGroupsS(name: String,
                            full: Option[Boolean] = None,
                            first: Int = 0,
                            limit: Int = Integer.MAX_VALUE,
                            batchSize: Int = 100): Observable[Group] = {

    val path = Seq(client.realm, "roles", name, "groups")
    client.getList[Group](path, createQuery(("full", full)), first, limit, batchSize)
  }
}

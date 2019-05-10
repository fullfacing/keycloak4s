package com.fullfacing.keycloak4s.admin.monix.services

import java.nio.ByteBuffer
import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.{Group, KeycloakError, User}
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Roles(implicit client: KeycloakClient) extends services.Roles[Task, Observable[ByteBuffer]] {

  /**
   *
   * @param clientId   The UUID of the client.
   * @param name       The name of the role.
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   * @return
   */
  def fetchClientRoleUsersS(first: Int = 0,
                           limit: Int = Integer.MAX_VALUE,
                           clientId: UUID,
                           name: String,
                           batchSize: Int = 100): Observable[Either[KeycloakError, Seq[User]]] = {

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
  def fetchClientRoleGroupsS(first: Int = 0,
                            limit: Int = Integer.MAX_VALUE,
                            clientId: UUID, name: String,
                            full: Option[Boolean],
                            batchSize: Int = 100): Observable[Either[KeycloakError, Seq[Group]]] = {

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
  def fetchRealmRoleUsersS(first: Int = 0,
                          limit: Int = Integer.MAX_VALUE,
                          name: String,
                          batchSize: Int = 100): Observable[Either[KeycloakError, Seq[User]]] = {

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
  def fetchRealmRoleGroupsS(first: Int = 0,
                           limit: Int = Integer.MAX_VALUE,
                           name: String,
                           full: Option[Boolean],
                           batchSize: Int = 100): Observable[Either[KeycloakError, Seq[Group]]] = {

    val path = Seq(client.realm, "roles", name, "groups")
    client.getList[Group](path, createQuery(("full", full)), first, limit, batchSize)
  }
}

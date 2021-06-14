package com.fullfacing.keycloak4s.admin.monix.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.UserSession
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Clients[T](implicit client: KeycloakClient[T]) extends services.Clients[Task] {

  /**
   * Get application offline sessions.
   * Returns a list of offline user sessions associated with this client.
   *
   * @param id         ID of client (not client-id).
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   */
  def fetchOfflineSessionsS(first: Int = 0,
                            limit: Int = Integer.MAX_VALUE,
                            id: UUID,
                            batchSize: Int = 100): Observable[UserSession] = {

    val path = Seq(client.realm, "clients", id.toString, "offline-sessions")
    client.getList[UserSession](path, offset = first, limit = limit, batch = batchSize)
  }

  /**
   * Get user sessions for client.
   * Returns a list of user sessions associated with this client.
   *
   * @param id         ID of client (not client-id).
   * @param first      Used for pagination, skips the specified number of sessions.
   * @param limit      The max amount of sessions to return.
   * @param batchSize  The amount of sessions each call should return.
   */
  def fetchUserSessionsS(first: Int = 0,
                         limit: Int = Integer.MAX_VALUE,
                         id: UUID,
                         batchSize: Int = 100): Observable[UserSession] = {

    val path = Seq(client.realm, "clients", id.toString, "user-sessions")
    client.getList[UserSession](path, offset = first, limit = limit, batch = batchSize)
  }
}

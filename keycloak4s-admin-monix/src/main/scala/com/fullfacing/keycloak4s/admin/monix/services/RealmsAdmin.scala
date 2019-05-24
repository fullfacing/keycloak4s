package com.fullfacing.keycloak4s.admin.monix.services

import java.nio.ByteBuffer

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.{AdminEvent, EventRepresentation, KeycloakError}
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class RealmsAdmin(implicit client: KeycloakClient) extends services.RealmsAdmin[Task, Observable[ByteBuffer]] {

  /** Returns all admin events, or filters events based on URL query parameters listed here. */
  def fetchAdminEventsS(first: Int = 0,
                        limit: Int = Integer.MAX_VALUE,
                        authClient: Option[String] = None,
                        authIpAddress: Option[String] = None,
                        authRealm: Option[String] = None,
                        authUser: Option[String] = None,
                        dateFrom: Option[String] = None,
                        dateTo: Option[String] = None,
                        operationTypes: Option[List[String]] = None,
                        resourcePath: Option[String] = None,
                        resourceTypes: Option[List[String]] = None,
                        batchSize: Int = 100): Observable[Either[KeycloakError, Seq[AdminEvent]]] = {

    val query = createQuery(
      ("authClient", authClient),
      ("authIpAddress", authIpAddress),
      ("authRealm", authRealm),
      ("authUser", authUser),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("operationTypes", toCsvList(operationTypes)),
      ("resourcePath", resourcePath),
      ("resourceTypes", toCsvList(resourceTypes))
    )

    val path = Seq(client.realm, "admin-events")
    client.getList[AdminEvent](path, query, first, limit, batchSize)
  }

  /** Returns all events, or filters them based on URL query parameters listed here. */
  def getEventsS(first: Int = 0,
                 limit: Int = Integer.MAX_VALUE,
                 clientName: Option[String] = None,
                 dateFrom: Option[String] = None,
                 dateTo: Option[String] = None,
                 ipAddress: Option[String] = None,
                 `type`: Option[List[String]] = None,
                 user: Option[String] = None,
                 batchSize: Int = 100): Observable[Either[KeycloakError, Seq[EventRepresentation]]] = {

    val query = createQuery(
      ("client", clientName),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("ipAddress", ipAddress),
      ("type", toCsvList(`type`)),
      ("user", user)
    )

    val path = Seq(client.realm, "events")
    client.getList[EventRepresentation](path, query, first, limit, batchSize)
  }
}
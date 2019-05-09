package com.fullfacing.keycloak4s.admin.monix.services

import java.nio.ByteBuffer

import com.fullfacing.keycloak4s.admin.models.User
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.KeycloakError
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Users(implicit client: KeycloakClient) extends services.Users[Task, Observable[ByteBuffer]] {

  /**
   * Get users Returns a list of users, filtered according to query parameters
   *
   * @param briefRep
   * @param email
   * @param first
   * @param firstName
   * @param lastName
   * @param search    A String contained in username, first or last name, or email
   * @param username
   * @return
   */
  def fetchB(first: Int = 0,
             limit: Int = Integer.MAX_VALUE,
             briefRep: Option[Boolean] = None,
             email: Option[String] = None,
             firstName: Option[String] = None,
             lastName: Option[String] = None,
             search: Option[String] = None,
             username: Option[String] = None,
             batchSize: Int = 100): Observable[Either[KeycloakError, Seq[User]]] = {

    val query = createQuery(
      ("briefRepresentation", briefRep),
      ("email", email),
      ("firstName", firstName),
      ("lastName", lastName),
      ("search", search),
      ("username", username)
    )

    val path = Seq(client.realm, "users")
    client.getList[User](path, query, first, limit, batchSize)
  }

  def fetchL(first: Int = 0,
             limit: Int = Integer.MAX_VALUE,
             briefRep: Option[Boolean] = None,
             email: Option[String] = None,
             firstName: Option[String] = None,
             lastName: Option[String] = None,
             search: Option[String] = None,
             username: Option[String] = None): Task[Either[KeycloakError, Seq[User]]] = {

    fetchB(first, limit, briefRep, email, firstName, lastName, search, username)
      .consumeWith(consumer[User]())
  }
}

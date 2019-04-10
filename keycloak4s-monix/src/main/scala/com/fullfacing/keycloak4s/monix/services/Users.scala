package com.fullfacing.keycloak4s.monix.services

import com.fullfacing.keycloak4s.models.{KeycloakError, User}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Users(implicit client: KeycloakClient) {

  /**
   * Get users Returns a list of users, filtered according to query parameters
   *
   * @param briefRep
   * @param email
   * @param first
   * @param firstName
   * @param lastName
   * @param max       Maximum results size (defaults to 100)
   * @param search    A String contained in username, first or last name, or email
   * @param username
   * @return
   */
  def getUsers(first: Option[Int] = None,
               max: Option[Int] = None,
               briefRep: Option[Boolean] = None,
               email: Option[String] = None,
               firstName: Option[String] = None,
               lastName: Option[String] = None,
               search: Option[String] = None,
               username: Option[String] = None,
               batchSize: Int = 100): Observable[User] = {

    val query = createQuery(
      ("briefRepresentation", briefRep),
      ("email", email),
      ("firstName", firstName),
      ("lastName", lastName),
      ("max", max),
      ("search", search),
      ("username", username)
    )

    val path = Seq(client.realm, "users")
    client.getList[User](path, query, first.getOrElse(0), batchSize)
  }

  def getUsersL(first: Option[Int] = None,
                max: Option[Int] = None,
                briefRep: Option[Boolean] = None,
                email: Option[String] = None,
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                search: Option[String] = None,
                username: Option[String] = None): Task[List[User]] = {

    getUsers(first, max, briefRep, email, firstName, lastName, search, username)
      .consumeWith(consumer[User]())
  }
}

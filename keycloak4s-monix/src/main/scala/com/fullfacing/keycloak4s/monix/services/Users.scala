package com.fullfacing.keycloak4s.monix.services

import com.fullfacing.keycloak4s.models.{KeycloakError, User}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.services.createQuery
import monix.eval.Task

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
  def getUsers(briefRep: Option[Boolean] = None,
               email: Option[String] = None,
               first: Option[Int] = None,
               firstName: Option[String] = None,
               lastName: Option[String] = None,
               max: Option[Int] = None,
               search: Option[String] = None,
               username: Option[String] = None): Task[Either[KeycloakError, List[User]]] = {

    val query = createQuery(
      ("briefRepresentation", briefRep),
      ("email", email),
      ("first", first),
      ("firstName", firstName),
      ("lastName", lastName),
      ("max", max),
      ("search", search),
      ("username", username)
    )

    val path = Seq(client.realm, "users")
    client.get[List[User]](path, query = query)
  }
}

package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.User

import scala.collection.immutable.Seq

object Users {

  private val resource = "users"

  /**
   *
   * @param realm
   * @param user
   * @return
   */
  def createUser(realm: String, user: User): AsyncApolloResponse[Unit] = { //TODO return type check
    val path = Seq(realm, resource)
    SttpClient.post(user, path)
  }

  /**
   *
   * @param realm     realm name (not id!)
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
  def getUsers(realm: String,
               briefRep: Option[Boolean],
               email: Option[String],
               first: Option[Int],
               firstName: Option[String],
               lastName: Option[String],
               max: Option[Int],
               search: Option[String],
               username: Option[String]): AsyncApolloResponse[List[User]] = {

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

    val path = Seq(realm, resource)
    SttpClient.get(path, query.to[Seq])
  }

  /**
   *
   * @param realm
   * @return
   */
  def getUsersCount(realm: String): AsyncApolloResponse[Int] = {
    val path = Seq(realm, resource, "count")
    SttpClient.get(path)
  }

  /**
   *
   * @param realm
   * @param userId
   * @return
   */
  def getUserById(realm: String, userId: String): AsyncApolloResponse[User] = {
    val path = Seq(realm, resource, userId)
    SttpClient.get[User](path)
  }
}

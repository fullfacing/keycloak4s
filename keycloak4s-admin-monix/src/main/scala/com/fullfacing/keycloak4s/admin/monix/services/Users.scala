package com.fullfacing.keycloak4s.admin.monix.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.core.models.{Group, User}
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq

class Users[T](implicit client: KeycloakClient[T]) extends services.Users[Task] {

  /**
   * Get all realm users. Returns a list of users, filtered according to query parameters
   *
   * @param briefRep
   * @param email
   * @param firstName
   * @param lastName
   * @param search     A String contained in username, first or last name, or email
   * @param username
   * @param first      Used for pagination, skips the specified number of users.
   * @param limit      The max amount of users to return.
   * @param batchSize  The amount of users each call should return.
   */
  def fetchS(first: Int = 0,
             limit: Int = Integer.MAX_VALUE,
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
             username: Option[String] = None): Task[Seq[User]] = {

    fetchS(first, limit, briefRep, email, firstName, lastName, search, username)
      .consumeWith(consumer())
  }

  /**
   *
   *
   * @param userId
   * @param search
   * @param first      Used for pagination, skips the specified number of groups.
   * @param limit      The max amount of groups to return.
   * @param batchSize  The amount of groups each call should return.
   * @return
   */
  def fetchGroupsS(userId: UUID,
                   first: Int = 0,
                   limit: Int = Integer.MAX_VALUE,
                   search: Option[String] = None,
                   batchSize: Int = 100): Observable[Group] = {

    val path = Seq(client.realm, "users", userId.toString, "groups")
    client.getList[Group](path, createQuery(("search", search)), first, limit, batchSize)
  }

  def fetchGroupsL(userId: UUID, first: Int = 0, limit: Int = Integer.MAX_VALUE, search: Option[String] = None): Task[Seq[Group]] = {
    fetchGroupsS(userId, first, limit, search)
      .consumeWith(consumer())
  }
}

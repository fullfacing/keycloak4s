package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient

import scala.collection.immutable.Seq

object AttackDetection {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @param realm Name of the Realm
   * @return
   */
  def clearAllLoginFailures(realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "attack-detection", "brute-force", "users")
    SttpClient.delete(path)
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param realm   Name of the Realm.
   * @param userId  ID of the User.
   * @return
   */
  def getUserStatus(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "attack-detection", "brute-force", "users", userId)
    SttpClient.get(path)
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param realm Name of the Realm.
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "attack-detection", "brute-force", "users", userId)
    SttpClient.delete(path)
  }
}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.BruteForceResponse
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

class AttackDetection[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @param realm Name of the Realm
   * @return
   */
  def clearAllLoginFailures(realm: String): R[Unit] = {
    client.delete(realm :: "attack-detection" :: "brute-force" :: "users" :: Nil, Seq.empty[KeyValue])
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param realm   Name of the Realm.
   * @param userId  ID of the User.
   * @return
   */
  def getUserStatus(realm: String, userId: String): R[BruteForceResponse] = {
    client.get[BruteForceResponse](realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil, Seq.empty[KeyValue])
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param realm Name of the Realm.
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure(realm: String, userId: String): R[Unit] = {
    client.delete(realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil, Seq.empty[KeyValue])
  }
}
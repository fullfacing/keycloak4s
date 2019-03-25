package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.BruteForceResponse

class AttackDetection[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @return
   */
  def clearAllLoginFailures(): R[Unit] = {
    client.delete(client.realm :: "attack-detection" :: "brute-force" :: "users" :: Nil)
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param userId  ID of the User.
   * @return
   */
  def getUserStatus(userId: String): R[BruteForceResponse] = {
    client.get[BruteForceResponse](client.realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil)
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure(userId: String): R[Unit] = {
    client.delete(client.realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil)
  }
}
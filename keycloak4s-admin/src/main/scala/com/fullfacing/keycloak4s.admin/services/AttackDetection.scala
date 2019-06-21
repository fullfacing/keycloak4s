package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.BruteForceResponse
import com.fullfacing.keycloak4s.core.models.KeycloakError

class AttackDetection[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @return
   */
  def clearAllLoginFailures(realm: String): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](realm :: "attack-detection" :: "brute-force" :: "users" :: Nil)
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param userId  ID of the User.
   * @return
   */
  def fetchUserStatus(realm: String, userId: UUID): R[Either[KeycloakError, BruteForceResponse]] = {
    client.get[BruteForceResponse](realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure(realm: String, userId: UUID): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }
}
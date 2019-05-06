package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{BruteForceResponse, KeycloakError}

class AttackDetection[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @return
   */
  def clearAllLoginFailures(): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](client.realm :: "attack-detection" :: "brute-force" :: "users" :: Nil)
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param userId  ID of the User.
   * @return
   */
  def getUserStatus(userId: UUID): R[Either[KeycloakError, BruteForceResponse]] = {
    client.get[BruteForceResponse](client.realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure(userId: UUID): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](client.realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }
}
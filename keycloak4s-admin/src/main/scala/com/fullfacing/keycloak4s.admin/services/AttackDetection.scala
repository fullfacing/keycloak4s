package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{BruteForceResponse, KeycloakError}

class AttackDetection[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /* Clear any login failures for all users. This can release temporary disabled users. **/
  def clearAllLoginFailures(realm: String): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](realm :: "attack-detection" :: "brute-force" :: "users" :: Nil)
  }

  /* Retrieves login failure details for a specified user. **/
  def fetchUserStatus(realm: String, userId: UUID): R[Either[KeycloakError, BruteForceResponse]] = {
    client.get[BruteForceResponse](realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }

  /* Clear any login failures for a specified user. **/
  def clearUserLoginFailure(realm: String, userId: UUID): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](realm :: "attack-detection" :: "brute-force" :: "users" :: userId.toString :: Nil)
  }
}
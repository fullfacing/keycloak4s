package com.fullfacing.keycloak4s.services

import cats.data.Kleisli
import com.fullfacing.keycloak4s.handles.KeycloakClient.Request
import com.fullfacing.keycloak4s.models.BruteForceResponse
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object AttackDetection {

  /**
   * Clear any user login failures for all users.
   * This can release temporary disabled users.
   *
   * @param realm Name of the Realm
   * @return
   */
  def clearAllLoginFailures[R[_], S](realm: String): Request[R, S, Unit] = Kleisli { client =>
    client.delete(realm :: "attack-detection" :: "brute-force" :: "users" :: Nil, Seq.empty[KeyValue])
  }

  /**
   * Get status of a username in brute force detection.
   *
   * @param realm   Name of the Realm.
   * @param userId  ID of the User.
   * @return
   */
  def getUserStatus[R[_], S](realm: String, userId: String): Request[R, S, BruteForceResponse] = Kleisli { client =>
    client.get[BruteForceResponse](realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil, Seq.empty[KeyValue])
  }

  /**
   * Clear any user login failures for the user.
   * This can release temporary disabled user.
   *
   * @param realm Name of the Realm.
   * @param userId  ID of the User.
   */
  def clearUserLoginFailure[R[_], S](realm: String, userId: String): Request[R, S, Unit] = Kleisli { client =>
    client.delete(realm :: "attack-detection" :: "brute-force" :: "users" :: userId :: Nil, Seq.empty[KeyValue])
  }
}
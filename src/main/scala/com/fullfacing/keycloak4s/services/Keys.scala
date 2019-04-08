package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{KeycloakError, KeysMetadata}

import scala.collection.immutable.Seq

class Keys[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val resource: String = "keys"

  /**
   *
   * @param realm
   * @return
   */
  def getRealmKeys(realm: String): R[Either[KeycloakError, KeysMetadata]] = {
    val path = Seq(realm, resource)
    client.get[KeysMetadata](path)
  }
}

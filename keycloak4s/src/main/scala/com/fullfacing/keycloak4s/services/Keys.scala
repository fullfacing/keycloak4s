package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.KeysMetadata

import scala.collection.immutable.Seq

class Keys[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   *
   */
  def getRealmKeys(): R[KeysMetadata] = {
    val path = Seq(client.realm, "keys")
    client.get[KeysMetadata](path)
  }
}

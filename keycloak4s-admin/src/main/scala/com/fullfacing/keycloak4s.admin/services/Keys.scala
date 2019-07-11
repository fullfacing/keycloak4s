package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, KeysMetadata}

import scala.collection.immutable.Seq

class Keys[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   *
   */
  def getRealmKeys(): R[Either[KeycloakError, KeysMetadata]] = {
    val path = Seq(client.realm, "keys")
    client.get[KeysMetadata](path)
  }
}

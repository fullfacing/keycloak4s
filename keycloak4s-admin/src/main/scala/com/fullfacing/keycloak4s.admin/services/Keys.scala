package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, KeysMetadata}

import scala.collection.immutable.Seq

class Keys[R[+_]: Concurrent](implicit client: KeycloakClient[R]) {

  /** Retrieves metadata of a Realm's keys. */
  def fetchRealmKeys(): R[Either[KeycloakError, KeysMetadata]] = {
    val path = Seq(client.realm, "keys")
    client.get[KeysMetadata](path)
  }
}

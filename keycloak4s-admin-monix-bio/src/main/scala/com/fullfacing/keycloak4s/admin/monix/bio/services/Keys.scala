package com.fullfacing.keycloak4s.admin.monix.bio.services

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, KeysMetadata}
import monix.bio.IO

import scala.collection.immutable.Seq

class Keys(implicit client: KeycloakClient) {

  /** Retrieves metadata of a Realm's keys. */
  def fetchRealmKeys(): IO[KeycloakError, KeysMetadata] = {
    val path = Seq(client.realm, "keys")
    client.get[KeysMetadata](path)
  }
}

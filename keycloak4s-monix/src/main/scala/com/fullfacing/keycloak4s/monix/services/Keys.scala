package com.fullfacing.keycloak4s.monix.services

import com.fullfacing.keycloak4s.models.{KeycloakError, KeysMetadata}
import com.fullfacing.keycloak4s.monix.client.KeycloakClient
import monix.eval.Task

import scala.collection.immutable.Seq

class Keys(implicit client: KeycloakClient) {

  /**
   *
   */
  def getRealmKeys(): Task[Either[KeycloakError, KeysMetadata]] = {
    val path = Seq(client.realm, "keys")
    client.get[KeysMetadata](path)
  }
}
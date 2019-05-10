package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.core.models.KeysMetadata
import com.fullfacing.keycloak4s.core.models.KeycloakError
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
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
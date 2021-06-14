package com.fullfacing.keycloak4s.admin.monix.bio.services

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, ServerInfo}
import monix.bio.IO

import scala.collection.immutable.Seq

class Root(implicit client: KeycloakClient) {

  /** Get themes, social providers, auth providers, and event listeners available on this server */
  def serverInfo: IO[KeycloakError, ServerInfo] = {
    client.get[ServerInfo](Seq.empty[String])
  }
}

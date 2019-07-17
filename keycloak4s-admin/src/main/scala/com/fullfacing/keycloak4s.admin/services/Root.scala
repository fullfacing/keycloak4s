package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, ServerInfo}

import scala.collection.immutable.Seq

class Root[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Get themes, social providers, auth providers, and event listeners available on this server */
  def serverInfo: R[Either[KeycloakError, ServerInfo]] = {
    client.get[ServerInfo](Seq.empty[String])
  }
}

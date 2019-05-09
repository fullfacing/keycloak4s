package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.models.ServerInfo
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class Root[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Get themes, social providers, auth providers, and event listeners available on this server */
  def serverInfo: R[Either[KeycloakError, ServerInfo]] = {
    client.get[ServerInfo](Seq.empty[String])
  }

//  def corsPreflight(path: Seq[String] = Seq.empty[String])(implicit authToken: String): R[Either[KeycloakError, UnknownResponse]] = { //TODO test call
//    client.options(path)
//  }
}

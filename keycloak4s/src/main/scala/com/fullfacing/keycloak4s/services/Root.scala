package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{KeycloakError, ServerInfo}

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

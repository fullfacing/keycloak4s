package com.fullfacing.keycloak4s.services

import cats.data.Kleisli
import com.fullfacing.keycloak4s.handles.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object ClientInitialAccess {

  type Request[R[_], -S, A] = Kleisli[R, KeycloakClient[R, S], A]

  /**
   * Create a new initial access token.
   *
   * @param realm   Name of the Realm.
   * @param config
   * @return
   */
  def createNewInitialAccessToken[R[_], S](realm: String, config: ClientInitialAccessCreate): Request[R, S, ClientInitialAccess] = Kleisli { client =>
    client.post(config, realm :: "clients-initial-access" :: Nil)
  }

  /**
    * Retrieve all access tokens for the Realm.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def getInitialAccessTokens[R[_], S](realm: String)(implicit authToken: String): Request[R, S, Seq[ClientInitialAccess]] = Kleisli { client =>
    client.get[Seq[ClientInitialAccess]](realm :: "clients-initial-access" :: Nil)
  }

  /**
    * Delete an initial access token.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def deleteInitialAccessToken[R[_], S](tokenId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    client.delete(realm :: "clients-initial-access" :: Nil)
  }
}

object Test {
  ClientInitialAccess.createNewInitialAccessToken()
}
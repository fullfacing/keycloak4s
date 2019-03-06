package com.fullfacing.keycloak4s.services

import cats.data.Kleisli
import com.fullfacing.keycloak4s.handles.KeycloakClient.Request
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object ClientInitialAccess {

  /**
   * Create a new initial access token.
   *
   * @param realm   Name of the Realm.
   * @param config
   * @return
   */
  def createNewInitialAccessToken[R[_], S](realm: String, config: ClientInitialAccessCreate): Request[R, S, ClientInitialAccess] = Kleisli { client =>
    client.post[ClientInitialAccessCreate, ClientInitialAccess](config, realm :: "clients-initial-access" :: Nil, Seq.empty[KeyValue])
  }

  /**
    * Retrieve all access tokens for the Realm.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def getInitialAccessTokens[R[_], S](realm: String): Request[R, S, Seq[ClientInitialAccess]] = Kleisli { client =>
    client.get[Seq[ClientInitialAccess]](realm :: "clients-initial-access" :: Nil, Seq.empty[KeyValue])
  }

  /**
    * Delete an initial access token.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def deleteInitialAccessToken[R[_], S](tokenId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    client.delete(realm :: "clients-initial-access" :: tokenId :: Nil, Seq.empty[KeyValue])
  }
}
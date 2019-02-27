package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object ClientInitialAccess {

  /**
   * Create a new initial access token.
   *
   * @param realm   Name of the Realm.
   * @param config
   * @return
   */
  def createNewInitialAccessToken(realm: String, config: ClientInitialAccessCreate): AsyncApolloResponse[ClientInitialAccess] = {
    val path = Seq(realm, "clients-initial-access")
    SttpClient.post(config, path)
  }

  /**
   * Retrieve all access tokens for the Realm.
   *
   * @param realm   Name of the Realm.
   * @return
   */
  def getInitialAccessTokens(realm: String): AsyncApolloResponse[Seq[ClientInitialAccess]] = {
    val path = Seq(realm, "clients-initial-access")
    SttpClient.get(path)
  }

  /**
   * Delete an initial access token.
   *
   * @param realm   Name of the Realm.
   * @return
   */
  def deleteInitialAccessToken(tokenId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients-initial-access")
    SttpClient.delete(path)
  }
}

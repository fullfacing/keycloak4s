package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.handles.SttpClient

import scala.collection.immutable.Seq

object Authenticate {

  def getAccessToken(realm: String, username: String, password: String, client_id: String): AsyncApolloResponse[AccessTokenResponse] = {
    val path = Seq(realm, "protocol", "openid-connect", "token")
    val body = Map(
      "username"   -> username,
      "password"   -> password,
      "client_id"  -> client_id,
      "grant_type" -> "password"
    )
    SttpClient.auth[AccessTokenResponse](body, path)
  }

  def refreshAccessToken(realm: String, refreshToken: String, client_id: String): AsyncApolloResponse[AccessTokenResponse] = {
    val path = Seq(realm, "protocol", "openid-connect", "token")
    val body = Map(
      "refresh_token" -> refreshToken,
      "client_id"     -> client_id,
      "grant_type"    -> "refresh_token"
    )
    SttpClient.auth[AccessTokenResponse](body, path)
  }

  case class AccessTokenResponse(access_token: String,
                                 expires_in: Int,
                                 refresh_expires_in: Int,
                                 refresh_token: String,
                                 token_type: String,
                                 session_state: String,
                                 scope: String)
}

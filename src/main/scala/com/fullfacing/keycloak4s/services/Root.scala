package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.ServerInfo

import scala.collection.immutable.Seq

object Root {

  /** Get themes, social providers, auth providers, and event listeners available on this server */
  def serverInfo: AsyncApolloResponse[ServerInfo] = {
    SttpClient.get(Seq.empty[String])
  }
}

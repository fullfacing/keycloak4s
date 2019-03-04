package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.SttpClient.UnknownResponse
import com.fullfacing.keycloak4s.models.ServerInfo

import scala.collection.immutable.Seq

object Root {

  /** Get themes, social providers, auth providers, and event listeners available on this server */
  def serverInfo(implicit authToken: String): AsyncApolloResponse[ServerInfo] = {
    SttpClient.get(Seq.empty[String])
  }

  def corsPreflight(path: Seq[String] = Seq.empty[String])(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = { //TODO test call
    SttpClient.options(path)
  }
}

package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.handles.SttpClient
import com.fullfacing.keycloak4s.models.KeysMetadata

import scala.collection.immutable.Seq

object Key {

  private val resource: String = "keys"

  /**
   *
   * @param realm
   * @return
   */
  def getRealmKeys(realm: String)(implicit authToken: String): AsyncApolloResponse[KeysMetadata] = {
    val path = Seq(realm, resource)
    SttpClient.get(path)
  }
}

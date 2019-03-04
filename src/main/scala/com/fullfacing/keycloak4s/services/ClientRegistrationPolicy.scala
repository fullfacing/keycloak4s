package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.ComponentType

import scala.collection.immutable.Seq

object ClientRegistrationPolicy {

  /**
   * Base path for retrieving providers with the configProperties properly filled.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getClientRegistrationPolicyProviders(realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ComponentType]] = {
    val path = Seq(realm, "client-registration-policy", "providers")
    SttpClient.get(path)
  }
}

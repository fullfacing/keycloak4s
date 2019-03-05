package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.handles.SttpClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object ClientScopes {

  /**
   * Create a new client scope.
   * Client Scope’s name must be unique!
   *
   * @param realm       Name of the Realm.
   * @param clientScope Object representing ClientScope details.
   * @return
   */
  def createNewClientScope(realm: String, clientScope: ClientScope)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "client-scopes")
    SttpClient.post(clientScope, path)
  }

  /**
   * Returns a list of client scopes belonging to the realm.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getRealmClientScopes(realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ClientScope]] = {
    val path = Seq(realm, "client-scopes")
    SttpClient.get(path)
  }

  /**
   * Get representation of the client scope.
   *
   * @param scopeId ID of the ClientScope.
   * @param realm   Name of the Realm.
   * @return
   */
  def getClientScope(scopeId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[ClientScope] = {
    val path = Seq(realm, "client-scopes", scopeId)
    SttpClient.get(path)
  }

  /**
   * Update a client scope.
   *
   * @param scopeId     ID of the ClientScope.
   * @param realm       Name of the Realm.
   * @param clientScope Object representing ClientScope details.
   * @return
   */
  def updateClientScope(scopeId: String, realm: String, clientScope: ClientScope)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "client-scopes", scopeId)
    SttpClient.put(clientScope, path)
  }

  /**
   * Delete a client scope.
   *
   * @param scopeId ID of the ClientScope.
   * @param realm   Name of the Realm.
   * @return
   */
  def deleteClientScope(scopeId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "client-scopes", scopeId)
    SttpClient.delete(path)
  }
}

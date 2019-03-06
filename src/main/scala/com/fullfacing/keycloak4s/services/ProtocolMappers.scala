//package com.fullfacing.keycloak4s.services
//
//import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
//import com.fullfacing.apollo.core.protocol.NoContent
//import com.fullfacing.keycloak4s.handles.KeycloakClient
//import com.fullfacing.keycloak4s.models._
//
//import scala.collection.immutable.Seq
//
//object ProtocolMappers {
//
//  /**
//   * Create multiple protocol mappers for a client scope.
//   *
//   * @param scopeId ID of client scope (not name).
//   * @param realm   Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def createMulitpleMappersForScope(scopeId: String, realm: String, mapper: Seq[ProtocolMapper])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "add-models")
//    SttpClient.post(mapper, path)
//  }
//
//  /**
//   * Create a protocol mapper for a client scope.
//   *
//   * @param scopeId ID of client scope (not name).
//   * @param realm   Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def createMapperForScope(scopeId: String, realm: String, mapper: ProtocolMapper)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models")
//    SttpClient.post(mapper, path)
//  }
//
//  /**
//   * Get protocol mappers belonging to a client scope.
//   *
//   * @param scopeId ID of client scope (not name).
//   * @param realm   Name of the Realm.
//   * @return
//   */
//  def getMappersForScope(scopeId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ProtocolMapper]] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers")
//    SttpClient.get(path)
//  }
//
//  /**
//   * Get protocol mapper by ID belonging to a client scope.
//   *
//   * @param scopeId   ID of client scope (not name).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def getMapperForScope(scopeId: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[ProtocolMapper] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
//    SttpClient.get(path)
//  }
//
//  /**
//   * Update a protocol mapper belonging to a client scope.
//   *
//   * @param scopeId   ID of client scope (not name).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def updateMapperForScope(scopeId: String, mapperId: String, realm: String, mapper: ProtocolMapper)(implicit authToken: String): AsyncApolloResponse[ProtocolMapper] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
//    SttpClient.put(mapper, path)
//  }
//
//  /**
//   * Delete mapper for identity provider belonging to a client scope.
//   *
//   * @param scopeId   ID of client scope (not name).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def deleteMapperForScope(scopeId: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
//    SttpClient.delete(path)
//  }
//
//  /**
//   * Get protocol mappers by name for a specific protocol belonging to a client scope.
//   *
//   * @param scopeId   ID of client scope (not name).
//   * @param protocol
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def getMappersByProtocolForScope(scopeId: String, protocol: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ProtocolMapper]] = {
//    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "protocol", protocol)
//    SttpClient.get(path)
//  }
//
//  /**
//   * Create multiple protocol mappers for a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param realm     Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def createMulitpleMappersForClient(clientId: String, realm: String, mapper: Seq[ProtocolMapper])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "add-models")
//    SttpClient.post(mapper, path)
//  }
//
//  /**
//   * Create a protocol mapper for a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param realm     Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def createMapperForClient(clientId: String, realm: String, mapper: ProtocolMapper)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models")
//    SttpClient.post(mapper, path)
//  }
//
//  /**
//   * Get protocol mappers belonging to a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def getMappersForClient(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ProtocolMapper]] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers")
//    SttpClient.get(path)
//  }
//
//  /**
//   * Get protocol mapper by ID belonging to a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def getMapperForClient(clientId: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[ProtocolMapper] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
//    SttpClient.get(path)
//  }
//
//  /**
//   * Update a protocol mapper belonging to a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @param mapper
//   * @return
//   */
//  def updateMapperForClient(clientId: String, mapperId: String, realm: String, mapper: ProtocolMapper)(implicit authToken: String): AsyncApolloResponse[ProtocolMapper] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
//    SttpClient.put(mapper, path)
//  }
//
//  /**
//   * Delete mapper for identity provider belonging to a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param mapperId
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def deleteMapperForClient(clientId: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
//    SttpClient.delete(path)
//  }
//
//  /**
//   * Get protocol mappers by name for a specific protocol belonging to a client.
//   *
//   * @param clientId  ID of client (not client-id).
//   * @param protocol
//   * @param realm     Name of the Realm.
//   * @return
//   */
//  def getMappersByProtocolForClient(clientId: String, protocol: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ProtocolMapper]] = {
//    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "protocol", protocol)
//    SttpClient.get(path)
//  }
//}

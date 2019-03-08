package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class ProtocolMappers[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Create multiple protocol mappers for a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @param realm   Name of the Realm.
   * @param mapper
   * @return
   */
  def createMulitpleMappersForScope(scopeId: String, realm: String, mapper: Seq[ProtocolMapper]): R[Unit] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "add-models")
    client.post[Seq[ProtocolMapper]](mapper, path)
  }

  /**
   * Create a protocol mapper for a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @param realm   Name of the Realm.
   * @param mapper
   * @return
   */
  def createMapperForScope(scopeId: String, realm: String, mapper: ProtocolMapper): R[Unit] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models")
    client.post[ProtocolMapper](mapper, path)
  }

  /**
   * Get protocol mappers belonging to a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @param realm   Name of the Realm.
   * @return
   */
  def getMappersForScope(scopeId: String, realm: String): R[Seq[ProtocolMapper]] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers")
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Get protocol mapper by ID belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def getMapperForScope(scopeId: String, mapperId: String, realm: String): R[ProtocolMapper] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /**
   * Update a protocol mapper belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @param mapper
   * @return
   */
  def updateMapperForScope(scopeId: String, mapperId: String, realm: String, mapper: ProtocolMapper): R[ProtocolMapper] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.put[ProtocolMapper, ProtocolMapper](mapper, path)
  }

  /**
   * Delete mapper for identity provider belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteMapperForScope(scopeId: String, mapperId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.delete(path)
  }

  /**
   * Get protocol mappers by name for a specific protocol belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param protocol
   * @param realm     Name of the Realm.
   * @return
   */
  def getMappersByProtocolForScope(scopeId: String, protocol: String, realm: String): R[Seq[ProtocolMapper]] = {
    val path = Seq(realm, "client-scopes", scopeId, "protocol-mappers", "protocol", protocol)
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Create multiple protocol mappers for a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param mapper
   * @return
   */
  def createMulitpleMappersForClient(clientId: String, realm: String, mapper: Seq[ProtocolMapper]): R[Unit] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "add-models")
    client.post[Seq[ProtocolMapper]](mapper, path)
  }

  /**
   * Create a protocol mapper for a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param mapper
   * @return
   */
  def createMapperForClient(clientId: String, realm: String, mapper: ProtocolMapper): R[Unit] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models")
    client.post[ProtocolMapper](mapper, path)
  }

  /**
   * Get protocol mappers belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def getMappersForClient(clientId: String, realm: String): R[Seq[ProtocolMapper]] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers")
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Get protocol mapper by ID belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def getMapperForClient(clientId: String, mapperId: String, realm: String): R[ProtocolMapper] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /**
   * Update a protocol mapper belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @param mapper
   * @return
   */
  def updateMapperForClient(clientId: String, mapperId: String, realm: String, mapper: ProtocolMapper): R[ProtocolMapper] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.put[ProtocolMapper, ProtocolMapper](mapper, path)
  }

  /**
   * Delete mapper for identity provider belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteMapperForClient(clientId: String, mapperId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.delete(path)
  }

  /**
   * Get protocol mappers by name for a specific protocol belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param protocol
   * @param realm     Name of the Realm.
   * @return
   */
  def getMappersByProtocolForClient(clientId: String, protocol: String, realm: String): R[Seq[ProtocolMapper]] = {
    val path = Seq(realm, "client-scopes", clientId, "protocol-mappers", "protocol", protocol)
    client.get[Seq[ProtocolMapper]](path)
  }
}

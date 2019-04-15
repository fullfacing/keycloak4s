package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class ProtocolMappers[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Create multiple protocol mappers for a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @param mapper
   * @return
   */
  def createMulitpleMappersForScope(scopeId: String, mapper: Seq[ProtocolMapper]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "add-models")
    client.post[Seq[ProtocolMapper], Unit](path, mapper)
  }

  /**
   * Create a protocol mapper for a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @param mapper
   * @return
   */
  def createMapperForScope(scopeId: String, mapper: ProtocolMapper): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "models")
    client.post[ProtocolMapper, Unit](path, mapper)
  }

  /**
   * Get protocol mappers belonging to a client scope.
   *
   * @param scopeId ID of client scope (not name).
   * @return
   */
  def getMappersForScope(scopeId: String): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers")
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Get protocol mapper by ID belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @return
   */
  def getMapperForScope(scopeId: String, mapperId: String): R[Either[KeycloakError, ProtocolMapper]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /**
   * Update a protocol mapper belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @param mapper
   * @return
   */
  def updateMapperForScope(scopeId: String, mapperId: String, mapper: ProtocolMapper): R[Either[KeycloakError, ProtocolMapper]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.put[ProtocolMapper, ProtocolMapper](path, mapper)
  }

  /**
   * Delete mapper for identity provider belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param mapperId
   * @return
   */
  def deleteMapperForScope(scopeId: String, mapperId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "models", mapperId)
    client.delete(path)
  }

  /**
   * Get protocol mappers by name for a specific protocol belonging to a client scope.
   *
   * @param scopeId   ID of client scope (not name).
   * @param protocol
   * @return
   */
  def getMappersByProtocolForScope(scopeId: String, protocol: String): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path = Seq(client.realm, "client-scopes", scopeId, "protocol-mappers", "protocol", protocol)
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Create multiple protocol mappers for a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapper
   * @return
   */
  def createMulitpleMappersForClient(clientId: String, mapper: Seq[ProtocolMapper]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "add-models")
    client.post[Seq[ProtocolMapper], Unit](path, mapper)
  }

  /**
   * Create a protocol mapper for a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapper
   * @return
   */
  def createMapperForClient(clientId: String, mapper: ProtocolMapper): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "models")
    client.post[ProtocolMapper, Unit](path, mapper)
  }

  /**
   * Get protocol mappers belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @return
   */
  def getMappersForClient(clientId: String): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers")
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Get protocol mapper by ID belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @return
   */
  def getMapperForClient(clientId: String, mapperId: String): R[Either[KeycloakError, ProtocolMapper]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /**
   * Update a protocol mapper belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @param mapper
   * @return
   */
  def updateMapperForClient(clientId: String, mapperId: String, mapper: ProtocolMapper): R[Either[KeycloakError, ProtocolMapper]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.put[ProtocolMapper, ProtocolMapper](path, mapper)
  }

  /**
   * Delete mapper for identity provider belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param mapperId
   * @return
   */
  def deleteMapperForClient(clientId: String, mapperId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "models", mapperId)
    client.delete(path)
  }

  /**
   * Get protocol mappers by name for a specific protocol belonging to a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param protocol
   * @return
   */
  def getMappersByProtocolForClient(clientId: String, protocol: String): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path = Seq(client.realm, "client-scopes", clientId, "protocol-mappers", "protocol", protocol)
    client.get[Seq[ProtocolMapper]](path)
  }
}

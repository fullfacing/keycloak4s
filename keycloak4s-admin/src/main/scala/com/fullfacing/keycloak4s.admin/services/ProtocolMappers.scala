package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import com.fullfacing.keycloak4s.core.models.enums.{Protocol, ProtocolMapperEntity}

import scala.collection.immutable.Seq

class ProtocolMappers[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Creates multiple protocol mappers for either a client or client scope. */
  def createMany(entityId: UUID, entity: ProtocolMapperEntity, mapper: Seq[ProtocolMapper.Create]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "add-models")
    client.post[Unit](path, mapper)
  }

  /**
   * Create a protocol mapper for either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param mapper   The ProtocolMappers to be created.
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   */
  def create(entityId: UUID, entity: ProtocolMapperEntity, mapper: ProtocolMapper.Create): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.post[Unit](path, mapper)
  }

  /**
   * Get protocol mappers belonging to either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   */
  def fetch(entityId: UUID, entity: ProtocolMapperEntity): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.get[Seq[ProtocolMapper]](path)
  }

  /**
   * Get protocol mapper by ID belonging to either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   * @param mapperId The ID of the ProtocolMapper to be fetched.
   */
  def fetchById(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): R[Either[KeycloakError, ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /**
   * NB: Based on testing, only the properties in the config field can be changed.
   *
   * Update a protocol mapper belonging to either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   * @param mapperId The ID of the ProtocolMapper to be updated.
   * @param mapper   The updated ProtocolMapper model.
   */
  def update(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID, mapper: ProtocolMapper.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.put[Unit](path, mapper)
  }

  /**
   * Delete mapper for identity provider belonging to either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   * @param mapperId The ID of the ProtocolMapper to be deleted.
   */
  def delete(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.delete[Unit](path)
  }

  /**
   * Get protocol mappers by name for a specific protocol belonging to either a client or client scope.
   *
   * @param entityId ID of the client or client-scope (depending on value of entity param).
   * @param entity   The type of entity this Protocol Mapper is linked to (Client or Client-Scope).
   * @param protocol Fetch ProtocolMappers with this protocol type.
   */
  def fetchByProtocol(entityId: UUID, entity: ProtocolMapperEntity, protocol: Protocol): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "protocol", protocol.value)
    client.get[Seq[ProtocolMapper]](path)
  }
}

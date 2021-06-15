package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import com.fullfacing.keycloak4s.core.models.enums.{Protocol, ProtocolMapperEntity}

import scala.collection.immutable.Seq

class ProtocolMappers[R[+_]: Concurrent](implicit client: KeycloakClient[R]) {

  /** Creates multiple protocol mappers for either a client or client scope. */
  def createMany(entityId: UUID, entity: ProtocolMapperEntity, mapper: Seq[ProtocolMapper.Create]): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "add-models")
    client.post[Unit](path, mapper)
  }

  /** Creates a protocol mapper for either a client or client scope. */
  def create(entityId: UUID, entity: ProtocolMapperEntity, mapper: ProtocolMapper.Create): R[Either[KeycloakError, UUID]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    Concurrent[R].map(client.post[Headers](path, mapper))(extractUuid)
  }

  /** Retrieves a list of protocol mappers belonging to either a client or client scope. */
  def fetch(entityId: UUID, entity: ProtocolMapperEntity): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.get[Seq[ProtocolMapper]](path)
  }

  /** Retrieves a protocol mapper by ID belonging to either a client or client scope. */
  def fetchById(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): R[Either[KeycloakError, ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /** Retrieves a list of protocol mappers by name for a specific protocol belonging to either a client or client scope. */
  def fetchByProtocol(entityId: UUID, entity: ProtocolMapperEntity, protocol: Protocol): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "protocol", protocol.value)
    client.get[Seq[ProtocolMapper]](path)
  }

  /** Updates a protocol mapper belonging to either a client or client scope. */
  def update(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID, mapper: ProtocolMapper.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.put[Unit](path, mapper)
  }

  /** Deletes a protocol mapper belonging to either a client or client scope. */
  def delete(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.delete[Unit](path)
  }
}

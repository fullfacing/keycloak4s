package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.enums.{Protocol, ProtocolMapperEntity}
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class ProtocolMappers(implicit client: KeycloakClient) {

  /** Creates multiple protocol mappers for either a client or client scope. */
  def createMany(entityId: UUID, entity: ProtocolMapperEntity, mapper: Seq[ProtocolMapper.Create]): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "add-models")
    client.post[Unit](path, mapper)
  }

  /** Creates a protocol mapper for either a client or client scope. */
  def create(entityId: UUID, entity: ProtocolMapperEntity, mapper: ProtocolMapper.Create): IO[KeycloakError, UUID] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.post[Headers](path, mapper).map(extractUuid).flatMap(IO.fromEither)
  }

  /** Retrieves a list of protocol mappers belonging to either a client or client scope. */
  def fetch(entityId: UUID, entity: ProtocolMapperEntity): IO[KeycloakError, Seq[ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.get[Seq[ProtocolMapper]](path)
  }

  /** Retrieves a protocol mapper by ID belonging to either a client or client scope. */
  def fetchById(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): IO[KeycloakError, ProtocolMapper] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /** Retrieves a list of protocol mappers by name for a specific protocol belonging to either a client or client scope. */
  def fetchByProtocol(entityId: UUID, entity: ProtocolMapperEntity, protocol: Protocol): IO[KeycloakError, Seq[ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "protocol", protocol.value)
    client.get[Seq[ProtocolMapper]](path)
  }

  /** Updates a protocol mapper belonging to either a client or client scope. */
  def update(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID, mapper: ProtocolMapper.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.put[Unit](path, mapper)
  }

  /** Deletes a protocol mapper belonging to either a client or client scope. */
  def delete(entityId: UUID, entity: ProtocolMapperEntity, mapperId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.delete[Unit](path)
  }
}

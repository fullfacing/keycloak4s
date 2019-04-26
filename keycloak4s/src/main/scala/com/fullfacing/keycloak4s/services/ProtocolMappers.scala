package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.fullfacing.keycloak4s.models.enums.ProtocolMapperEntity

import scala.collection.immutable.Seq

class ProtocolMappers[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Create multiple protocol mappers for either a client or client scope. */
  def createMany(entityId: UUID, mapper: Seq[ProtocolMapper.Create], entity: ProtocolMapperEntity): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "add-models")
    client.post[Unit](path, mapper)
  }

  /** Create a protocol mapper for either a client or client scope. */
  def create(entityId: UUID, mapper: ProtocolMapper.Create, entity: ProtocolMapperEntity): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.post[Unit](path, mapper)
  }

  /** Get protocol mappers belonging to either a client or client scope. */
  def fetch(entityId: UUID, entity: ProtocolMapperEntity): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models")
    client.get[Seq[ProtocolMapper]](path)
  }

  /** Get protocol mapper by ID belonging to either a client or client scope. */
  def fetchById(entityId: UUID, mapperId: UUID, entity: ProtocolMapperEntity): R[Either[KeycloakError, ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.get[ProtocolMapper](path)
  }

  /** Update a protocol mapper belonging to either a client or client scope. */
  def update(entityId: UUID, mapperId: UUID, mapper: ProtocolMapper.Update, entity: ProtocolMapperEntity): R[Either[KeycloakError, ProtocolMapper]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.put[ProtocolMapper](path, mapper)
  }

  /** Delete mapper for identity provider belonging to either a client or client scope. */
  def delete(entityId: UUID, mapperId: UUID, entity: ProtocolMapperEntity): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "models", mapperId)
    client.delete[Unit](path)
  }

  /** Get protocol mappers by name for a specific protocol belonging to either a client or client scope. */
  def fetchByProtocol(entityId: UUID, protocol: String, entity: ProtocolMapperEntity): R[Either[KeycloakError, Seq[ProtocolMapper]]] = {
    val path: Path = Seq(client.realm, entity.value, entityId, "protocol-mappers", "protocol", protocol)
    client.get[Seq[ProtocolMapper]](path)
  }
}

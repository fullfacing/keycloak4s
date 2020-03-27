package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq

class Components[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Creates a component. */
  def create(component: Component.Create): R[Either[KeycloakError, UUID]] = {
    val path: Path = Seq(client.realm, "components")
    Concurrent[R].map(client.post[Headers](path, component))(extractUuid)
  }

  /** Retrieves a list of components for a Realm. */
  def fetch(name: Option[String], parent: Option[String], `type`: Option[String]): R[Either[KeycloakError, Seq[Component]]] = {
    val path: Path = Seq(client.realm, "components")
    val query = createQuery(("name", name), ("parent",parent), ("type",`type`))
    client.get[Seq[Component]](path, query)
  }

  /** Retrieves a component by id. */
  def fetchById(componentId: UUID): R[Either[KeycloakError, Component]] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.get[Component](path)
  }

  /** Updates a component. */
  def update(componentId: UUID, component: Component.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.put[Unit](path, component)
  }

  /** Deletes a component. */
  def delete(componentId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.delete[Unit](path)
  }

  /** Retrieves a list of sub component types that are available to configure for a particular parent component. */
  def fetchSubTypes(componentId: UUID, `type`: String): R[Either[KeycloakError, Seq[ComponentType]]] = {
    val path: Path = Seq(client.realm, "components", componentId, "sub-component-types")
    val query = Seq(KeyValue("type", `type`))
    client.get[Seq[ComponentType]](path, query = query)
  }
}

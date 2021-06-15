package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq

class Components(implicit client: KeycloakClient) {

  /** Creates a component. */
  def create(component: Component.Create): IO[KeycloakError, UUID] = {
    val path: Path = Seq(client.realm, "components")
    client.post[Headers](path, component).map(extractUuid).flatMap(IO.fromEither)
  }

  /** Retrieves a list of components for a Realm. */
  def fetch(name: Option[String], parent: Option[String], `type`: Option[String]): IO[KeycloakError, Seq[Component]] = {
    val path: Path = Seq(client.realm, "components")
    val query = createQuery(("name", name), ("parent",parent), ("type",`type`))
    client.get[Seq[Component]](path, query)
  }

  /** Retrieves a component by id. */
  def fetchById(componentId: UUID): IO[KeycloakError, Component] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.get[Component](path)
  }

  /** Updates a component. */
  def update(componentId: UUID, component: Component.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.put[Unit](path, component)
  }

  /** Deletes a component. */
  def delete(componentId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "components", componentId)
    client.delete[Unit](path)
  }

  /** Retrieves a list of sub component types that are available to configure for a particular parent component. */
  def fetchSubTypes(componentId: UUID, `type`: String): IO[KeycloakError, Seq[ComponentType]] = {
    val path: Path = Seq(client.realm, "components", componentId, "sub-component-types")
    val query = Seq(KeyValue("type", `type`))
    client.get[Seq[ComponentType]](path, query = query)
  }
}

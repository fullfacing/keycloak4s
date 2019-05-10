package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class Components[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Create a component.
   *
   * @param component Object representing a component's details.
   * @return
   */
  def createComponent(component: Component.Create): R[Either[KeycloakError, Unit]] = {
    client.post[Unit](client.realm :: "components" :: Nil, component)
  }

  /**
   * Retrieves all components for a Realm.
   */
  def fetchComponents(name: Option[String], parent: Option[String], `type`: Option[String]): R[Either[KeycloakError, Seq[Component]]] = {
    val query = createQuery(("name", name), ("parent",parent), ("type",`type`))
    client.get[Seq[Component]](client.realm :: "components" :: Nil, query)
  }

  /**
   * Retrieves a component.
   *
   * @param componentId ID of the component.
   * @return
   */
  def fetchComponent(componentId: UUID): R[Either[KeycloakError, Component]] = {
    client.get[Component](client.realm :: "components" :: componentId.toString :: Nil)
  }

  /**
   * Updates a component.
   *
   * @param componentId ID of the component.
   * @param component   Object representing a component's details.
   * @return
   */
  def updateComponent(componentId: UUID, component: Component.Update): R[Either[KeycloakError, Component]] = {
    client.put[Component](client.realm :: "components" :: componentId.toString :: Nil, component)
  }

  /**
   * Deletes a component.
   *
   * @param componentId ID of the component.
   * @return
   */
  def deleteComponent(componentId: UUID): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](client.realm :: "components" :: componentId.toString :: Nil)
  }

  /**
   * Retrieves list of subcomponent types that are available to configure for a particular parent component.
   *
   * @param componentId ID of the component.
   * @param `type`
   * @return
   */
  def fetchSubComponentTypes(componentId: UUID, `type`: Option[String] = None): R[Either[KeycloakError, Seq[ComponentType]]] = {
    val query = createQuery(("type", `type`))
    client.get[Seq[ComponentType]](client.realm :: "components" :: componentId.toString :: "sub-component-types" :: Nil, query = query)
  }
}

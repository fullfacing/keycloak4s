package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Components[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Create a component.
   *
   * @param component Object representing a component's details.
   * @return
   */
  def createComponent(component: Component): R[Unit] = {
    client.post[Component, Unit](client.realm :: "components" :: Nil, component)
  }

  /**
   * Retrieves all components for a Realm.
   */
  def getComponents(): R[Seq[Component]] = {
    client.get[Seq[Component]](client.realm :: "components" :: Nil)
  }

  /**
   * Retrieves a component.
   *
   * @param componentId ID of the component.
   * @return
   */
  def getComponent(componentId: String): R[Component] = {
    client.get[Component](client.realm :: "components" :: componentId :: Nil)
  }

  /**
   * Updates a component.
   *
   * @param componentId ID of the component.
   * @param component   Object representing a component's details.
   * @return
   */
  def updateComponent(componentId: String, component: Component): R[Component] = {
    client.put[Component, Component](client.realm :: "components" :: componentId :: Nil, component)
  }

  /**
   * Deletes a component.
   *
   * @param componentId ID of the component.
   * @return
   */
  def deleteComponent(componentId: String): R[Unit] = {
    client.delete(client.realm :: "components" :: componentId :: Nil)
  }

  /**
   * Retrieves list of subcomponent types that are available to configure for a particular parent component.
   *
   * @param componentId ID of the component.
   * @param `type`
   * @return
   */
  def getListOfSubComponentTypes(componentId: String, `type`: Option[String] = None): R[Seq[ComponentType]] = {
    val query = createQuery(("type", `type`))
    client.get[Seq[ComponentType]](client.realm :: "components" :: componentId :: "sub-component-types" :: Nil, query = query)
  }
}

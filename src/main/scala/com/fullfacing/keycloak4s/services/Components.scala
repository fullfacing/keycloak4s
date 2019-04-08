package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Components[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Create a component.
   *
   * @param realm     Name of the Realm.
   * @param component Object representing a component's details.
   * @return
   */
  def createComponent(realm: String, component: Component): R[Either[KeycloakError, Unit]] = {
    client.post[Component, Unit](realm :: "components" :: Nil, component)
  }

  /**
   * Retrieves all components for a Realm.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getComponents(realm: String): R[Either[KeycloakError, Seq[Component]]] = {
    client.get[Seq[Component]](realm :: "components" :: Nil)
  }

  /**
   * Retrieves a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @return
   */
  def getComponent(componentId: String, realm: String): R[Either[KeycloakError, Component]] = {
    client.get[Component](realm :: "components" :: componentId :: Nil)
  }

  /**
   * Updates a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @param component   Object representing a component's details.
   * @return
   */
  def updateComponent(componentId: String, realm: String, component: Component): R[Either[KeycloakError, Component]] = {
    client.put[Component, Component](realm :: "components" :: componentId :: Nil, component)
  }

  /**
   * Deletes a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteComponent(componentId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    client.delete(realm :: "components" :: componentId :: Nil)
  }

  /**
   * Retrieves list of subcomponent types that are available to configure for a particular parent component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @param `type`
   * @return
   */
  def getListOfSubComponentTypes(componentId: String, realm: String, `type`: Option[String] = None): R[Either[KeycloakError, Seq[ComponentType]]] = {
    val query = createQuery(("type", `type`))
    client.get[Seq[ComponentType]](realm :: "components" :: componentId :: "sub-component-types" :: Nil, query = query)
  }
}

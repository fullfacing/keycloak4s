package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

object Components {

  /**
   * Create a component.
   *
   * @param realm     Name of the Realm.
   * @param component Object representing a component's details.
   * @return
   */
  def createComponent(realm: String, component: Component)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "components")
    SttpClient.post(component, path)
  }

  /**
   * Retrieves all components for a Realm.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getComponents(realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[Component]] = {
    val path = Seq(realm, "components")
    SttpClient.get(path)
  }

  /**
   * Retrieves a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @return
   */
  def getComponent(componentId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Component] = {
    val path = Seq(realm, "components", componentId)
    SttpClient.get(path)
  }

  /**
   * Updates a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @param component   Object representing a component's details.
   * @return
   */
  def updateComponent(componentId: String, realm: String, component: Component)(implicit authToken: String): AsyncApolloResponse[Component] = {
    val path = Seq(realm, "components", componentId)
    SttpClient.put(component, path)
  }

  /**
   * Deletes a component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteComponent(componentId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "components", componentId)
    SttpClient.delete(path)
  }

  /**
   * Retrieves list of subcomponent types that are available to configure for a particular parent component.
   *
   * @param componentId ID of the component.
   * @param realm       Name of the Realm.
   * @param `type`
   * @return
   */
  def getListOfSubComponentTypes(componentId: String, realm: String, `type`: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[Seq[ComponentType]] = {
    val query = createQuery(("type", `type`))

    val path = Seq(realm, "components", componentId, "sub-component-types")
    SttpClient.get(path, query.to[Seq])
  }
}

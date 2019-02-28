package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object RealmsAdmin {

  /**
   * Imports a realm from a full representation of that realm.
   *
   * @param realm Representation of the realm.
   * @return
   */
  def importRealm(realm: RealmRepresentation): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq.empty[String]
    SttpClient.post(realm, path)
  }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getTopLevelRepresentation(realm: String): AsyncApolloResponse[RealmRepresentation] = {
    val path = Seq(realm)
    SttpClient.get(path)
  }

  /**
   * Update the top-level representation of the realm.
   * Any user, roles or client information in the representation will be ignored.
   *
   * @param realm   Name of the Realm.
   * @param update  Representation of the realm.
   * @return
   */
  def updateTopLevelRepresentation(realm: String, update: RealmRepresentation): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm)
    SttpClient.put(update, path)
  }

  /**
   * Delete the realm.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def deleteRealm(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm)
    SttpClient.delete(path)
  }

  /**
   * Returns all admin events, or filters events based on URL query parameters listed here.
   *
   * @param realm           Name of the Realm.
   * @param authClient
   * @param authIpAddress
   * @param authRealm
   * @param authUser        User ID, not name.
   * @param dateFrom
   * @param dateTo
   * @param first
   * @param max             Maximum results size (defaults to 100)
   * @param operationTypes
   * @param resourcePath
   * @param resourceTypes
   * @return
   */
  def getAdminEvents(realm: String,
                     authClient: Option[String] = None,
                     authIpAddress: Option[String] = None,
                     authRealm: Option[String] = None,
                     authUser: Option[String] = None,
                     dateFrom: Option[String] = None,
                     dateTo: Option[String] = None,
                     first: Option[Int] = None,
                     max: Option[Int] = None,
                     operationTypes: Option[Seq[String]] = None,
                     resourcePath: Option[String] = None,
                     resourceTypes: Option[Seq[String]] = None): AsyncApolloResponse[AdminEvent] = {
    val query = createQuery(
      ("authClient", authClient),
      ("authIpAddress", authIpAddress),
      ("authRealm", authRealm),
      ("authUser", authUser),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("first", first),
      ("max", max),
      ("operationTypes", operationTypes),
      ("resourcePath", resourcePath),
      ("resourceTypes", resourceTypes)
    )

    val path = Seq(realm, "admin-events")
    SttpClient.get(path, query)
  }

  /**
   * Delete all admin events.
   *
   * @param realm Name of the realm.
   * @return
   */
  def deleteAdminEvents(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "admin-events")
    SttpClient.delete(path)
  }

  /**
   * Clear cache of external public keys (Public keys of clients or Identity providers).
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearKeysCache(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clear-keys-cache")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Clears realm cache.
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearRealmCache(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clear-realm-cache")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Clears user cache.
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearUserCache(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clear-user-cache")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  //TODO Determine what exactly this route does, as the description does not correlate well with the route name.
  // As well determine the input, for consumers they list not only application/json, but application/xml and text/plain as well.
  /**
   * Base path for importing clients under this realm.
   *
   * @param realm       Name of the realm.
   * @param description
   * @return
   */
  def convertClientDescription(realm: String, description: String): AsyncApolloResponse[Client] = {
    val path = Seq(realm, "client-description-converter")
    SttpClient.post(description, path)
  }

  /**
   * Get client session stats.
   * The key is the client id, the value is the number of sessions that currently are active with that client.
   * Only clients that actually have a session associated with them will be in this map.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getClientSessionStats(realm: String): AsyncApolloResponse[Seq[Map[String, Any]]] = { //TODO Determine return type.
    val path = Seq(realm, "client-session-stats")
    SttpClient.get(path)
  }

  /**
   * Get realm default client scopes.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getDefaultClientScopes(realm: String): AsyncApolloResponse[Seq[ClientScope]] = {
    val path = Seq(realm, "default-default-client-scopes")
    SttpClient.get(path)
  }

  /**
   * Update a default client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def updateDefaultClientScope(scopeId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId)
    SttpClient.put(path, Seq.empty[KeyValue])
  }

  /**
   * Deletes a default client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteDefaultClientScope(scopeId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId)
    SttpClient.delete(path)
  }

  /**
   * Get group hierarchy.
   * Only name and ids are returned.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getGroupHierarchy(realm: String): AsyncApolloResponse[Seq[Group]] = {
    val path = Seq(realm, "default-groups")
    SttpClient.get(path)
  }

  /**
   * Updates group hierarchy.
   *
   * @param groupId
   * @param realm   Name of the realm.
   * @return
   */
  def updateGroupHierarchy(groupId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-groups", groupId)
    SttpClient.put(path, Seq.empty[KeyValue])
  }

  /**
   * Deletes group hierarchy.
   *
   * @param groupId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteGroupHierarchy(groupId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-groups", groupId)
    SttpClient.delete(path)
  }

  /**
   * Get realm optional client scopes.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getOptionalClientScopes(realm: String): AsyncApolloResponse[Seq[ClientScope]] = {
    val path = Seq(realm, "default-Optional-client-scopes")
    SttpClient.get(path)
  }

  /**
   * Update a optional client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def updateOptionalClientScope(scopeId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-Optional-client-scopes", scopeId)
    SttpClient.put(path, Seq.empty[KeyValue])
  }

  /**
   * Deletes a optional client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteOptionalClientScope(scopeId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "default-Optional-client-scopes", scopeId)
    SttpClient.delete(path)
  }

  /**
   * Returns all events, or filters them based on URL query parameters listed here.
   *
   * @param realm     Name of the realm.
   * @param client    App or oauth client name.
   * @param dateFrom  From date.
   * @param dateTo    To Date.
   * @param first     Paging offset.
   * @param ipAddress IP address.
   * @param max       Maximum results size (defaults to 100).
   * @param `type`    The types of events to return
   * @param user      User id.
   * @return
   */
  def getEvents(realm: String,
                client: Option[String] = None,
                dateFrom: Option[String] = None,
                dateTo: Option[String] = None,
                first: Option[Int] = None,
                ipAddress: Option[String] = None,
                max: Option[Int] = None,
                `type`: Option[Seq[String]] = None,
                user: Option[String] = None): AsyncApolloResponse[Seq[EventRepresentation]] = {
    val query = createQuery(
      ("client", client),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("first", first),
      ("ipAddress", ipAddress),
      ("max", max),
      ("type", `type`),
      ("user", user)
    )

    val path = Seq(realm, "events")
    SttpClient.get(path, query)
  }
}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class RealmsAdmin[R[_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  /**
   * Imports a realm from a full representation of that realm.
   *
   * @param realm Representation of the realm.
   * @return
   */
  def importRealm(realm: RealmRepresentation): R[Response] = {
    val path = Seq.empty[String]
    keycloakClient.post[RealmRepresentation, Response](realm, path)
  }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getTopLevelRepresentation(realm: String): R[RealmRepresentation] = {
    val path = Seq(realm)
    keycloakClient.get[RealmRepresentation](path)
  }

  /**
   * Update the top-level representation of the realm.
   * Any user, roles or client information in the representation will be ignored.
   *
   * @param realm   Name of the Realm.
   * @param update  Representation of the realm.
   * @return
   */
  def updateTopLevelRepresentation(realm: String, update: RealmRepresentation): R[Response] = {
    val path = Seq(realm)
    keycloakClient.put[RealmRepresentation, Response](update, path)
  }

  /**
   * Delete the realm.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def deleteRealm(realm: String): R[Unit] = {
    val path = Seq(realm)
    keycloakClient.delete(path)
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
                     resourceTypes: Option[Seq[String]] = None): R[AdminEvent] = {
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
    keycloakClient.get[AdminEvent](path, query)
  }

  /**
   * Delete all admin events.
   *
   * @param realm Name of the realm.
   * @return
   */
  def deleteAdminEvents(realm: String): R[Unit] = {
    val path = Seq(realm, "admin-events")
    keycloakClient.delete(path)
  }

  /**
   * Clear cache of external public keys (Public keys of clients or Identity providers).
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearKeysCache(realm: String): R[Unit] = {
    val path = Seq(realm, "clear-keys-cache")
    keycloakClient.post(path)
  }

  /**
   * Clears realm cache.
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearRealmCache(realm: String): R[Unit] = {
    val path = Seq(realm, "clear-realm-cache")
    keycloakClient.post(path)
  }

  /**
   * Clears user cache.
   *
   * @param realm Name of the realm.
   * @return
   */
  def clearUserCache(realm: String): R[Unit] = {
    val path = Seq(realm, "clear-user-cache")
    keycloakClient.post(path)
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
  def convertClientDescription(realm: String, description: String): R[Client] = {
    val path = Seq(realm, "client-description-converter")
    keycloakClient.post(description, path)
  }

  /**
   * Get client session stats.
   * The key is the client id, the value is the number of sessions that currently are active with that client.
   * Only clients that actually have a session associated with them will be in this map.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getClientSessionStats(realm: String): R[Seq[ClientSessionStatistics]] = { //TODO Determine return type.
    val path = Seq(realm, "client-session-stats")
    keycloakClient.get[Seq[ClientSessionStatistics]](path)
  }

  /**
   * Get realm default client scopes.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getDefaultClientScopes(realm: String): R[Seq[ClientScope]] = {
    val path = Seq(realm, "default-default-client-scopes")
    keycloakClient.get[Seq[ClientScope]](path)
  }

  /**
   * Update a default client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def updateDefaultClientScope(scopeId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId)
    keycloakClient.put(path)
  }

  /**
   * Deletes a default client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteDefaultClientScope(scopeId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId)
    keycloakClient.delete(path)
  }

  /**
   * Get group hierarchy.
   * Only name and ids are returned.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getGroupHierarchy(realm: String): R[Seq[Group]] = {
    val path = Seq(realm, "default-groups")
    keycloakClient.get[Seq[Group]](path)
  }

  /**
   * Updates group hierarchy.
   *
   * @param groupId
   * @param realm   Name of the realm.
   * @return
   */
  def updateGroupHierarchy(groupId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-groups", groupId)
    keycloakClient.put(path)
  }

  /**
   * Deletes group hierarchy.
   *
   * @param groupId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteGroupHierarchy(groupId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-groups", groupId)
    keycloakClient.delete(path)
  }

  /**
   * Get realm optional client scopes.
   *
   * @param realm Name of the realm.
   * @return
   */
  def getOptionalClientScopes(realm: String): R[Seq[ClientScope]] = {
    val path = Seq(realm, "default-Optional-client-scopes")
    keycloakClient.get[Seq[ClientScope]](path)
  }

  /**
   * Update a optional client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def updateOptionalClientScope(scopeId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-Optional-client-scopes", scopeId)
    keycloakClient.put(path)
  }

  /**
   * Deletes a optional client scope.
   *
   * @param scopeId
   * @param realm   Name of the realm.
   * @return
   */
  def deleteOptionalClientScope(scopeId: String, realm: String): R[Unit] = {
    val path = Seq(realm, "default-Optional-client-scopes", scopeId)
    keycloakClient.delete(path)
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
                user: Option[String] = None): R[Seq[EventRepresentation]] = {
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
    keycloakClient.get[Seq[EventRepresentation]](path, query)
  }

  /**
   * Delete all events
   *
   * @param realm realm name (not id!)
   * @return
   */
  def deleteAllEvents(realm: String): R[Unit] = {
    val path = Seq(realm, "events")
    keycloakClient.delete(path)
  }

  /**
   * Get the events provider configuration
   * Returns JSON object with events provider configuration
   *
   * @param realm realm name (not id!)
   * @return
   */
  def getEventsConfig(realm: String): R[RealmEventsConfig] = {
    val path = Seq(realm, "events", "config")
    keycloakClient.get[RealmEventsConfig](path)
  }

  /**
   * Update the events provider.
   * Change the events provider and/or its configuration
   *
   * @param realm realm name (not id!)
   * @param config
   * @return
   */
  def updateEventsConfig(realm: String, config: RealmEventsConfig): R[Unit] = {
    val path = Seq(realm, "events", "config")
    keycloakClient.put(config, path)
  }

  /**
   * GET /{realm}/group-by-path/{path}
   *
   * @param realm realm name (not id!)
   * @param path
   * @return
   */
  def getGroupByPath(realm: String, path: String): R[Group] = {
    keycloakClient.get[Group](Seq(realm, "group-by-path", path))
  }

  /**
   * Removes all user sessions.
   * Any client that has an admin url will also be told to invalidate any sessions they have.
   *
   * @param realm realm name (not id!)
   * @return
   */
  def logoutAll(realm: String): R[GlobalRequestResult] = {
    val path = Seq(realm, "logout-all")
    keycloakClient.post[GlobalRequestResult](path)
  }

  /**
   * Partial export of existing realm into a JSON file.
   *
   * @param realm realm name (not id!)
   * @param exportClients
   * @param exportGroupsAndRoles
   * @return
   */
  def partialExport(realm: String,
                    exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): R[RealmRepresentation] = {
    val path    = Seq(realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    keycloakClient.post[RealmRepresentation](path, queries)
  }

  /**
   * Partial import from a JSON file to an existing realm.
   *
   * @param realm realm name (not id!)
   * @param rep
   * @return
   */
  def partialImport(realm: String, rep: PartialImport): R[Response] = {
    val path = Seq(realm, "partialImport")
    keycloakClient.post[PartialImport, Response](rep, path)
  }

  /**
   * Push the realm’s revocation policy to any client that has an admin url associated with it.
   *
   * @param realm realm name (not id!)
   * @return
   */
  def pushRevocation(realm: String): R[GlobalRequestResult] = {
    val path = Seq(realm, "push-revocation")
    keycloakClient.post[GlobalRequestResult](path)
  }

  /**
   * Remove a specific user session.
   * Any client that has an admin url will also be told to invalidate this particular session.
   *
   * @param realm   realm name (not id!)
   * @param session
   * @return
   */
  def removeUserSession(realm: String, session: String): R[Unit] = {
    val path = Seq(realm, "sessions", session)
    keycloakClient.delete(path)
  }

  /**
   * Test LDAP connection
   *
   * @param realm             realm name (not id!)
   * @param action
   * @param bindCredential
   * @param bindDn
   * @param componentId
   * @param connectionTimeout
   * @param connectionUrl
   * @param useTruststoreSpi
   * @return
   */
  def testLdapConnection(realm: String, // TODO Figure out how queries should be sent - FormData
                         action: Option[String] = None,
                         bindCredential: Option[String] = None,
                         bindDn: Option[String] = None,
                         componentId: Option[String] = None,
                         connectionTimeout: Option[String] = None,
                         connectionUrl: Option[String] = None,
                         useTruststoreSpi: Option[String] = None): R[Response] = {

    val path = Seq(realm, "testLDAPConnection")
    val queries = createQuery(
      ("action", action),
      ("bindCredential", bindCredential),
      ("bindDn", bindDn),
      ("componentId", componentId),
      ("connectionTimeout", connectionTimeout),
      ("connectionUrl", connectionUrl),
      ("useTruststoreSpi", useTruststoreSpi)
    )

    keycloakClient.post[Response](path, queries)
  }

  /**
   * Test SMTP connection with current logged in user
   *
   * @param realm  realm name (not id!)
   * @param config SMTP server configuration
   * @return
   */
  def testSmtpConnection(realm: String, config: String): R[Response] = {
    val path = Seq(realm, "testSMTPConnection", config)
    keycloakClient.post[Response](path)
  }

  /**
   * GET /{realm}/users-management-permissions
   *
   * @param realm realm name (not id!)
   * @return
   */
  def getUsersManagementPermissions(realm: String): R[ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    keycloakClient.get[ManagementPermission](path)
  }

  /**
   * PUT /{realm}/users-management-permissions
   *
   * @param realm realm name (not id!)
   * @param ref
   * @return
   */
  def updateUsersManagementPermissions(realm: String, ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    keycloakClient.put[ManagementPermission, ManagementPermission](ref, path)
  }
}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Multipart

import scala.collection.immutable.Seq

class RealmsAdmin[R[_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  /**
   * Imports a realm from a full representation of that realm.
   */
  def importRealm(realm: RealmRepresentation): R[Unit] = {
    val path = Seq.empty[String]
    keycloakClient.post[RealmRepresentation, Unit](path, realm)
  }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   *
   * @return
   */
  def getTopLevelRepresentation(): R[RealmRepresentation] = {
    val path = Seq(keycloakClient.realm)
    keycloakClient.get[RealmRepresentation](path)
  }

  /**
   * Update the top-level representation of the realm.
   * Any user, roles or client information in the representation will be ignored.
   *
   * @param update  Representation of the realm.
   * @return
   */
  def updateTopLevelRepresentation(update: RealmRepresentation): R[Unit] = {
    val path = Seq(keycloakClient.realm)
    keycloakClient.put[RealmRepresentation, Unit](path, update)
  }

  /**
   * Delete the realm.
   */
  def deleteRealm(): R[Unit] = {
    val path = Seq(keycloakClient.realm)
    keycloakClient.delete(path)
  }

  /**
   * Returns all admin events, or filters events based on URL query parameters listed here.
   *
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
  def getAdminEvents(authClient: Option[String] = None,
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

    val path = Seq(keycloakClient.realm, "admin-events")
    keycloakClient.get[AdminEvent](path, query = query)
  }

  /**
   * Delete all admin events.
   *
   * @return
   */
  def deleteAdminEvents(): R[Unit] = {
    val path = Seq(keycloakClient.realm, "admin-events")
    keycloakClient.delete(path)
  }

  /**
   * Clear cache of external public keys (Public keys of clients or Identity providers).
   *
   * @return
   */
  def clearKeysCache(): R[Unit] = {
    val path = Seq(keycloakClient.realm, "clear-keys-cache")
    keycloakClient.post(path)
  }

  /**
   * Clears realm cache.
   *
   * @return
   */
  def clearRealmCache(): R[Unit] = {
    val path = Seq(keycloakClient.realm, "clear-realm-cache")
    keycloakClient.post(path)
  }

  /**
   * Clears user cache.
   *
   * @return
   */
  def clearUserCache(): R[Unit] = {
    val path = Seq(keycloakClient.realm, "clear-user-cache")
    keycloakClient.post(path)
  }

  /**
   * Base path for importing clients under this realm.
   *
   * @param description
   * @return
   */
  def importClientViaDescription(description: String): R[Client] = {
    val path = Seq(keycloakClient.realm, "client-description-converter")
    keycloakClient.post[String, Client](path, description)
  }

  /**
   * Get client session stats.
   * The key is the client id, the value is the number of sessions that currently are active with that client.
   * Only clients that actually have a session associated with them will be in this map.
   *
   * @return
   */
  def getClientSessionStats(): R[Seq[ClientSessionStatistics]] = {
    val path = Seq(keycloakClient.realm, "client-session-stats")
    keycloakClient.get[Seq[ClientSessionStatistics]](path)
  }

  /**
   * Get realm default client scopes.
   *
   * @return
   */
  def getDefaultClientScopes(): R[Seq[ClientScope]] = {
    val path = Seq(keycloakClient.realm, "default-default-client-scopes")
    keycloakClient.get[Seq[ClientScope]](path)
  }

  /**
   * Update a default client scope.
   *
   * @param scopeId
   * @return
   */
  def updateDefaultClientScope(scopeId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-default-client-scopes", scopeId)
    keycloakClient.put(path)
  }

  /**
   * Deletes a default client scope.
   *
   * @param scopeId
   * @return
   */
  def deleteDefaultClientScope(scopeId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-default-client-scopes", scopeId)
    keycloakClient.delete(path)
  }

  /**
   * Get group hierarchy.
   * Only name and ids are returned.
   *
   * @return
   */
  def getGroupHierarchy(): R[Seq[Group]] = {
    val path = Seq(keycloakClient.realm, "default-groups")
    keycloakClient.get[Seq[Group]](path)
  }

  /**
   * Updates group hierarchy.
   *
   * @param groupId
   * @return
   */
  def updateGroupHierarchy(groupId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-groups", groupId)
    keycloakClient.put(path)
  }

  /**
   * Deletes group hierarchy.
   *
   * @param groupId
   * @return
   */
  def deleteGroupHierarchy(groupId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-groups", groupId)
    keycloakClient.delete(path)
  }

  /**
   * Get realm optional client scopes.
   */
  def getOptionalClientScopes(): R[Seq[ClientScope]] = {
    val path = Seq(keycloakClient.realm, "default-Optional-client-scopes")
    keycloakClient.get[Seq[ClientScope]](path)
  }

  /**
   * Update a optional client scope.
   *
   * @param scopeId
   * @return
   */
  def updateOptionalClientScope(scopeId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-Optional-client-scopes", scopeId)
    keycloakClient.put(path)
  }

  /**
   * Deletes a optional client scope.
   *
   * @param scopeId
   * @return
   */
  def deleteOptionalClientScope(scopeId: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "default-Optional-client-scopes", scopeId)
    keycloakClient.delete(path)
  }

  /**
   * Returns all events, or filters them based on URL query parameters listed here.
   *
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
  def getEvents(client: Option[String] = None,
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

    val path = Seq(keycloakClient.realm, "events")
    keycloakClient.get[Seq[EventRepresentation]](path, query = query)
  }

  /**
   * Delete all events
   *
   * @return
   */
  def deleteAllEvents(): R[Unit] = {
    val path = Seq(keycloakClient.realm, "events")
    keycloakClient.delete(path)
  }

  /**
   * Get the events provider configuration
   * Returns JSON object with events provider configuration
   *
   * @return
   */
  def getEventsConfig(): R[RealmEventsConfig] = {
    val path = Seq(keycloakClient.realm, "events", "config")
    keycloakClient.get[RealmEventsConfig](path)
  }

  /**
   * Update the events provider.
   * Change the events provider and/or its configuration
   *
   * @param config
   * @return
   */
  def updateEventsConfig(config: RealmEventsConfig): R[Unit] = {
    val path = Seq(keycloakClient.realm, "events", "config")
    keycloakClient.put(path, config)
  }

  /**
   * GET /{realm}/group-by-path/{path}
   *
   * @param path
   * @return
   */
  def getGroupByPath(path: String): R[Group] = {
    keycloakClient.get[Group](Seq(keycloakClient.realm, "group-by-path", path))
  }

  /**
   * Removes all user sessions.
   * Any client that has an admin url will also be told to invalidate any sessions they have.
   *
   * @return
   */
  def logoutAll(): R[GlobalRequestResult] = {
    val path = Seq(keycloakClient.realm, "logout-all")
    keycloakClient.post[Unit, GlobalRequestResult](path)
  }

  /**
   * Partial export of existing realm into a JSON file.
   *
   * @param exportClients
   * @param exportGroupsAndRoles
   * @return
   */
  def partialExport(exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): R[RealmRepresentation] = {
    val path    = Seq(keycloakClient.realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    keycloakClient.post[Unit, RealmRepresentation](path, query = queries)
  }

  /**
   * Partial import from a JSON file to an existing realm.
   *
   * @param rep
   * @return
   */
  def partialImport(rep: PartialImport): R[Unit] = {
    val path = Seq(keycloakClient.realm, "partialImport")
    keycloakClient.post[PartialImport, Unit](path, rep)
  }

  /**
   * Push the realm’s revocation policy to any client that has an admin url associated with it.
   *
   * @return
   */
  def pushRevocation(): R[GlobalRequestResult] = {
    val path = Seq(keycloakClient.realm, "push-revocation")
    keycloakClient.post[Unit, GlobalRequestResult](path)
  }

  /**
   * Remove a specific user session.
   * Any client that has an admin url will also be told to invalidate this particular session.
   *
   * @param session
   * @return
   */
  def removeUserSession(session: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "sessions", session)
    keycloakClient.delete(path)
  }

  /**
   * Test LDAP connection
   *
   * @param action
   * @param bindCredential
   * @param bindDn
   * @param componentId
   * @param connectionTimeout
   * @param connectionUrl
   * @param useTruststoreSpi
   * @return
   */
  def testLdapConnection(action: Option[String] = None,
                         bindCredential: Option[String] = None,
                         bindDn: Option[String] = None,
                         componentId: Option[String] = None,
                         connectionTimeout: Option[String] = None,
                         connectionUrl: Option[String] = None,
                         useTruststoreSpi: Option[String] = None): R[Unit] = {

    val path = Seq(keycloakClient.realm, "testLDAPConnection")
    val queries = Map(
      "action"            -> action,
      "bindCredential"    -> bindCredential,
      "bindDn"            -> bindDn,
      "componentId"       -> componentId,
      "connectionTimeout" -> connectionTimeout,
      "connectionUrl"     -> connectionUrl,
      "useTruststoreSpi"  -> useTruststoreSpi
    )

    //Documentation does not specify which content type this endpoint consumes, multipart/form-data and application/json are equally likely.
    //Therefor, in case the endpoint returns an error, it may be required to build a case class from the query parameters instead of a multipart.
    val mp = createMultipart(flattenOptionMap(queries))
    keycloakClient.post[Multipart, Unit](path, mp)
  }

  /**
   * Test SMTP connection with current logged in user
   *
   * @param config SMTP server configuration
   * @return
   */
  def testSmtpConnection(config: String): R[Unit] = {
    val path = Seq(keycloakClient.realm, "testSMTPConnection", config)
    keycloakClient.post(path)
  }

  /**
   * GET /{realm}/users-management-permissions
   */
  def getUsersManagementPermissions(): R[ManagementPermission] = {
    val path = Seq(keycloakClient.realm, "users-management-permissions")
    keycloakClient.get[ManagementPermission](path)
  }

  /**
   * PUT /{realm}/users-management-permissions
   *
   * @param ref
   * @return
   */
  def updateUsersManagementPermissions(ref: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(keycloakClient.realm, "users-management-permissions")
    keycloakClient.put[ManagementPermission, ManagementPermission](path, ref)
  }
}

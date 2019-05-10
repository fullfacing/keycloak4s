package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class RealmsAdmin[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Imports a realm from a full representation of that realm.
   */
  def importRealm(realm: RealmRepresentation.Create): R[Either[KeycloakError, Unit]] = {
    val path = Seq.empty[String]
    client.post[Unit](path, realm)
  }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   *
   * @return
   */
  def getTopLevelRepresentation(): R[Either[KeycloakError, RealmRepresentation]] = {
    val path = Seq(client.realm)
    client.get[RealmRepresentation](path)
  }

  /**
   * Update the top-level representation of the realm.
   * Any user, roles or client information in the representation will be ignored.
   *
   * @param update  Representation of the realm.
   * @return
   */
  def updateTopLevelRepresentation(update: RealmRepresentation): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm)
    client.put[Unit](path, update)
  }

  /**
   * Delete the realm.
   */
  def deleteRealm(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm)
    client.delete[Unit](path)
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
                     resourceTypes: Option[Seq[String]] = None): R[Either[KeycloakError, AdminEvent]] = {
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

    val path = Seq(client.realm, "admin-events")
    client.get[AdminEvent](path, query = query)
  }

  /**
   * Delete all admin events.
   *
   * @return
   */
  def deleteAdminEvents(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "admin-events")
    client.delete[Unit](path)
  }

  /**
   * Clear cache of external public keys (Public keys of clients or Identity providers).
   *
   * @return
   */
  def clearKeysCache(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clear-keys-cache")
    client.post[Unit](path)
  }

  /**
   * Clears realm cache.
   *
   * @return
   */
  def clearRealmCache(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clear-realm-cache")
    client.post[Unit](path)
  }

  /**
   * Clears user cache.
   *
   * @return
   */
  def clearUserCache(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clear-user-cache")
    client.post[Unit](path)
  }

  /**
   * Base path for importing clients under this realm.
   *
   * @param description
   * @return
   */
  def importClientViaDescription(description: String): R[Either[KeycloakError, Client]] = {
    val path = Seq(client.realm, "client-description-converter")
    client.post[Client](path, description)
  }

  /**
   * Get client session stats.
   * The key is the client id, the value is the number of sessions that currently are active with that client.
   * Only clients that actually have a session associated with them will be in this map.
   *
   * @return
   */
  def getClientSessionStats(): R[Either[KeycloakError, Seq[ClientSessionStatistics]]] = {
    val path = Seq(client.realm, "client-session-stats")
    client.get[Seq[ClientSessionStatistics]](path)
  }

  /**
   * Get realm default client scopes.
   *
   * @return
   */
  def getDefaultClientScopes(): R[Either[KeycloakError, Seq[ClientScope]]] = {
    val path = Seq(client.realm, "default-default-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /**
   * Update a default client scope.
   *
   * @param scopeId
   * @return
   */
  def updateDefaultClientScope(scopeId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-default-client-scopes", scopeId)
    client.put[Unit](path)
  }

  /**
   * Deletes a default client scope.
   *
   * @param scopeId
   * @return
   */
  def deleteDefaultClientScope(scopeId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-default-client-scopes", scopeId)
    client.delete[Unit](path)
  }

  /**
   * Get group hierarchy.
   * Only name and ids are returned.
   *
   * @return
   */
  def getGroupHierarchy(): R[Either[KeycloakError, Seq[Group]]] = {
    val path = Seq(client.realm, "default-groups")
    client.get[Seq[Group]](path)
  }

  /**
   * Updates group hierarchy.
   *
   * @param groupId
   * @return
   */
  def updateGroupHierarchy(groupId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-groups", groupId)
    client.put[Unit](path)
  }

  /**
   * Deletes group hierarchy.
   *
   * @param groupId
   * @return
   */
  def deleteGroupHierarchy(groupId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-groups", groupId)
    client.delete[Unit](path)
  }

  /**
   * Get realm optional client scopes.
   */
  def getOptionalClientScopes(): R[Either[KeycloakError, Seq[ClientScope]]] = {
    val path = Seq(client.realm, "default-Optional-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /**
   * Update a optional client scope.
   *
   * @param scopeId
   * @return
   */
  def updateOptionalClientScope(scopeId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-Optional-client-scopes", scopeId)
    client.put[Unit](path)
  }

  /**
   * Deletes a optional client scope.
   *
   * @param scopeId
   * @return
   */
  def deleteOptionalClientScope(scopeId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "default-Optional-client-scopes", scopeId)
    client.delete[Unit](path)
  }

  /**
   * Returns all events, or filters them based on URL query parameters listed here.
   *
   * @param clientName  App or oauth client name.
   * @param dateFrom    From date.
   * @param dateTo      To Date.
   * @param first       Paging offset.
   * @param ipAddress   IP address.
   * @param max         Maximum results size (defaults to 100).
   * @param `type`      The types of events to return
   * @param user        User id.
   * @return
   */
  def getEvents(clientName: Option[String] = None,
                dateFrom: Option[String] = None,
                dateTo: Option[String] = None,
                first: Option[Int] = None,
                ipAddress: Option[String] = None,
                max: Option[Int] = None,
                `type`: Option[Seq[String]] = None,
                user: Option[String] = None): R[Either[KeycloakError, Seq[EventRepresentation]]] = {
    val query = createQuery(
      ("client", clientName),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("first", first),
      ("ipAddress", ipAddress),
      ("max", max),
      ("type", `type`),
      ("user", user)
    )

    val path = Seq(client.realm, "events")
    client.get[Seq[EventRepresentation]](path, query = query)
  }

  /**
   * Delete all events
   *
   * @return
   */
  def deleteAllEvents(): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "events")
    client.delete[Unit](path)
  }

  /**
   * Get the events provider configuration
   * Returns JSON object with events provider configuration
   *
   * @return
   */
  def getEventsConfig(): R[Either[KeycloakError, RealmEventsConfig]] = {
    val path = Seq(client.realm, "events", "config")
    client.get[RealmEventsConfig](path)
  }

  /**
   * Update the events provider.
   * Change the events provider and/or its configuration
   *
   * @param config
   * @return
   */
  def updateEventsConfig(config: RealmEventsConfig): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "events", "config")
    client.put[Unit](path, config)
  }

  /**
   * GET /{realm}/group-by-path/{path}
   *
   * @param path
   * @return
   */
  def getGroupByPath(path: String): R[Either[KeycloakError, Group]] = {
    client.get[Group](Seq(client.realm, "group-by-path", path))
  }

  /**
   * Removes all user sessions.
   * Any client that has an admin url will also be told to invalidate any sessions they have.
   *
   * @return
   */
  def logoutAll(): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(client.realm, "logout-all")
    client.post[GlobalRequestResult](path)
  }

  /**
   * Partial export of existing realm into a JSON file.
   *
   * @param exportClients
   * @param exportGroupsAndRoles
   * @return
   */
  def partialExport(exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): R[Either[KeycloakError, RealmRepresentation]] = {
    val path    = Seq(client.realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    client.post[RealmRepresentation](path, query = queries)
  }

  /**
   * Partial import from a JSON file to an existing realm.
   *
   * @param rep
   * @return
   */
  def partialImport(rep: PartialImport): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "partialImport")
    client.post[Unit](path, rep)
  }

  /**
   * Push the realm’s revocation policy to any client that has an admin url associated with it.
   *
   * @return
   */
  def pushRevocation(): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(client.realm, "push-revocation")
    client.post[GlobalRequestResult](path)
  }

  /**
   * Remove a specific user session.
   * Any client that has an admin url will also be told to invalidate this particular session.
   *
   * @param session
   * @return
   */
  def removeUserSession(session: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "sessions", session)
    client.delete[Unit](path)
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
                         useTruststoreSpi: Option[String] = None): R[Either[KeycloakError, Unit]] = {

    val path = Seq(client.realm, "testLDAPConnection")
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
    client.post[Unit](path, mp)
  }

  /**
   * Test SMTP connection with current logged in user
   *
   * @param config SMTP server configuration
   * @return
   */
  def testSmtpConnection(config: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "testSMTPConnection", config)
    client.post[Unit](path)
  }

  /**
   * GET /{realm}/users-management-permissions
   */
  def getUsersManagementPermissions(): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "users-management-permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * PUT /{realm}/users-management-permissions
   *
   * @param ref
   * @return
   */
  def updateUsersManagementPermissions(ref: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "users-management-permissions")
    client.put[ManagementPermission](path, ref)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------ Client Registration Policies ------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //

  /** Base path for retrieving providers with the configProperties properly filled. */
  def getClientRegistrationPolicyProviders(): R[Either[KeycloakError, List[ComponentType]]] = {
    val path = Seq(client.realm, "client-registration-policy", "providers")
    client.get[List[ComponentType]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------- Initial Access Tokens ---------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Create a new initial access token. */
  def createInitialAccessToken(config: ClientInitialAccessCreate): R[Either[KeycloakError, ClientInitialAccess]] = {
    client.post[ClientInitialAccess](client.realm :: "clients-initial-access" :: Nil, config)
  }

  /** Retrieve all access tokens for the Realm. */
  def fetchInitialAccessTokens(): R[Either[KeycloakError, Seq[ClientInitialAccess]]] = {
    client.get[Seq[ClientInitialAccess]](client.realm :: "clients-initial-access" :: Nil)
  }

  /** Delete an initial access token. */
  def deleteInitialAccessToken(tokenId: String): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](client.realm :: "clients-initial-access" :: tokenId :: Nil)
  }
}

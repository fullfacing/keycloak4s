package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class RealmsAdmin[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Imports a realm from a full representation of that realm.
   */
  def create(realm: RealmRepresentation.Create): R[Either[KeycloakError, Unit]] = {
    val path = Seq.empty[String]
    client.post[Unit](path, realm)
  }

  def createAndRetrieve(realm: RealmRepresentation.Create): R[Either[KeycloakError, RealmRepresentation]] =
    Concurrent[R].flatMap(create(realm)) { _ =>
      fetch(realm.realm)
    }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   */
  def fetch(realm: String = client.realm): R[Either[KeycloakError, RealmRepresentation]] = {
    val path = Seq(realm)
    client.get[RealmRepresentation](path)
  }

  /**
   * Get the top-level representation of the realm.
   * It will not include nested information like User and Client representations.
   */
  def fetchAll(): R[Either[KeycloakError, List[RealmRepresentation]]] = {
    val path = Seq()
    client.get[List[RealmRepresentation]](path)
  }

  /**
   * Update the top-level representation of the realm.
   * Any user, roles or client information in the representation will be ignored.
   *
   * @param update  Representation of the realm.
   */
  def update(update: RealmRepresentation.Update, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm)
    client.put[Unit](path, update)
  }

  /**
   * Delete the realm.
   */
  def delete(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm)
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
  def fetchAdminEvents(realm: String = client.realm,
                       authClient: Option[String] = None,
                       authIpAddress: Option[String] = None,
                       authRealm: Option[String] = None,
                       authUser: Option[String] = None,
                       dateFrom: Option[String] = None,
                       dateTo: Option[String] = None,
                       first: Option[Int] = None,
                       max: Option[Int] = None,
                       operationTypes: Option[List[String]] = None,
                       resourcePath: Option[String] = None,
                       resourceTypes: Option[List[String]] = None): R[Either[KeycloakError, List[AdminEvent]]] = {

    val query = createQuery(
      ("authClient", authClient),
      ("authIpAddress", authIpAddress),
      ("authRealm", authRealm),
      ("authUser", authUser),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("first", first),
      ("max", max),
      ("operationTypes", toCsvList(operationTypes)),
      ("resourcePath", resourcePath),
      ("resourceTypes", toCsvList(resourceTypes))
    )

    val path = Seq(realm, "admin-events")
    client.get[List[AdminEvent]](path, query = query)
  }

  /** Delete all admin events. */
  def deleteAdminEvents(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "admin-events")
    client.delete[Unit](path)
  }

  /** Clear cache of external public keys (Public keys of clients or Identity providers). */
  def clearKeysCache(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "clear-keys-cache")
    client.post[Unit](path)
  }

  /** Clears realm cache. */
  def clearRealmCache(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "clear-realm-cache")
    client.post[Unit](path)
  }

  /** Clears user cache. */
  def clearUserCache(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "clear-user-cache")
    client.post[Unit](path)
  }

  /* PURPOSE OR USE CASES FOR THIS ENDPOINT IS NOT YET KNOWN **/
  def clientDescriptionConverter(description: String, realm: String = client.realm): R[Either[KeycloakError, Client]] = {
    val path = Seq(realm, "client-description-converter")
    client.post[Client](path, description)
  }

  /**
   * Get client session stats.
   * The key is the client id, the value is the number of sessions that currently are active with that client.
   * Only clients that actually have a session associated with them will be in this map.
   *
   * @return
   */
  def fetchClientSessionStats(realm: String = client.realm): R[Either[KeycloakError, Seq[ClientSessionStatistics]]] = {
    val path = Seq(realm, "client-session-stats")
    client.get[Seq[ClientSessionStatistics]](path)
  }

  /**
   * Get realm default client scopes.
   *
   * @return
   */
  def fetchDefaultClientScopes(realm: String = client.realm): R[Either[KeycloakError, Seq[ClientScope]]] = {
    val path = Seq(realm, "default-default-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /** Update a default client scope. */
  def assignClientScopeAsDefault(scopeId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId.toString)
    client.put[Unit](path)
  }

  /** Deletes a default client scope. */
  def unassignClientScopeAsDefault(scopeId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId.toString)
    client.delete[Unit](path)
  }

  /** Returns all default groups. Only name, path and ids are returned.  */
  def fetchDefaultGroups(realm: String = client.realm): R[Either[KeycloakError, Seq[Group]]] = {
    val path = Seq(realm, "default-groups")
    client.get[Seq[Group]](path)
  }

  /** Assigns a Group as default. */
  def assignGroupAsDefault(groupId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-groups", groupId.toString)
    client.put[Unit](path)
  }

  /** Unassigns a Group as default. */
  def unassignGroupAsDefault(groupId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-groups", groupId.toString)
    client.delete[Unit](path)
  }

  /** Get realm optional client scopes. */
  def fetchOptionalClientScopes(realm: String = client.realm): R[Either[KeycloakError, Seq[ClientScope]]] = {
    val path = Seq(realm, "default-optional-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /** Update a optional client scope. */
  def assignClientScopeAsOptional(scopeId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-optional-client-scopes", scopeId.toString)
    client.put[Unit](path)
  }

  /** Deletes a optional client scope. */
  def unassignClientScopeAsOptional(scopeId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "default-optional-client-scopes", scopeId.toString)
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
  def fetchEvents(realm: String = client.realm,
                  clientName: Option[String] = None,
                  dateFrom: Option[String] = None,
                  dateTo: Option[String] = None,
                  first: Option[Int] = None,
                  ipAddress: Option[String] = None,
                  max: Option[Int] = None,
                  `type`: Option[List[String]] = None,
                  user: Option[String] = None): R[Either[KeycloakError, Seq[EventRepresentation]]] = {

    val query = createQuery(
      ("client", clientName),
      ("dateFrom", dateFrom),
      ("dateTo", dateTo),
      ("first", first),
      ("ipAddress", ipAddress),
      ("max", max),
      ("type", toCsvList(`type`)),
      ("user", user)
    )

    val path = Seq(realm, "events")
    client.get[Seq[EventRepresentation]](path, query = query)
  }

  /** Delete all events */
  def deleteAllEvents(realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "events")
    client.delete[Unit](path)
  }

  /**
   * Get the events provider configuration
   * Returns JSON object with events provider configuration
   */
  def fetchEventsConfig(realm: String = client.realm): R[Either[KeycloakError, RealmEventsConfig]] = {
    val path = Seq(realm, "events", "config")
    client.get[RealmEventsConfig](path)
  }

  /**
   * Update the events provider.
   * Change the events provider and/or its configuration.
   */
  def updateEventsConfig(config: RealmEventsConfig.Update, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "events", "config")
    client.put[Unit](path, config)
  }

  /**
   * GET /{realm}/group-by-path/{path}
   *
   * @param path
   */
  def fetchGroupByPath(path: String, realm: String = client.realm): R[Either[KeycloakError, Group]] = {
    client.get[Group](Seq(realm, "group-by-path", path))
  }

  /**
   * Removes all user sessions.
   * Any client that has an admin url will also be told to invalidate any sessions they have.
   */
  def logoutAll(realm: String = client.realm): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(realm, "logout-all")
    client.post[GlobalRequestResult](path)
  }

  /**
   * Partial export of existing realm into a JSON file.
   *
   * @param exportClients
   * @param exportGroupsAndRoles
   * @return
   */
  def partialExport(realm: String = client.realm,
                    exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): R[Either[KeycloakError, RealmRepresentation]] = {
    val path    = Seq(realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    client.post[RealmRepresentation](path, query = queries)
  }

  /** Partial import from a JSON file to an existing realm. */
  def partialImport(rep: PartialImport, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "partialImport")
    client.post[Unit](path, rep)
  }

  /** Push the realm’s revocation policy to any client that has an admin url associated with it. */
  def pushRevocation(realm: String = client.realm): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(realm, "push-revocation")
    client.post[GlobalRequestResult](path)
  }

  /**
   * Remove a specific user session.
   * Any client that has an admin url will also be told to invalidate this particular session.
   */
  def removeUserSession(session: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "sessions", session.toString)
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
  def testLdapConnection(realm: String = client.realm,
                         action: Option[String] = None,
                         bindCredential: Option[String] = None,
                         bindDn: Option[String] = None,
                         componentId: Option[String] = None,
                         connectionTimeout: Option[String] = None,
                         connectionUrl: Option[String] = None,
                         useTruststoreSpi: Option[String] = None): R[Either[KeycloakError, Unit]] = {

    val path = Seq(realm, "testLDAPConnection")
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
  def testSmtpConnection(config: String, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "testSMTPConnection", config)
    client.post[Unit](path)
  }

  /**
   * GET /{realm}/users-management-permissions
   */
  def fetchUsersManagementPermissions(realm: String = client.realm): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "users-management-permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * PUT /{realm}/users-management-permissions
   *
   * @param ref
   * @return
   */
  def updateUsersManagementPermissions(ref: ManagementPermission.Update, realm: String = client.realm): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "users-management-permissions")
    client.put[ManagementPermission](path, ref)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------ Client Registration Policies ------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //

  /** Base path for retrieving providers with the configProperties properly filled. */
  def fetchClientRegistrationPolicyProviders(realm: String = client.realm): R[Either[KeycloakError, List[ComponentType]]] = {
    val path = Seq(realm, "client-registration-policy", "providers")
    client.get[List[ComponentType]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------- Initial Access Tokens ---------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Create a new initial access token. */
  def createInitialAccessToken(config: ClientInitialAccessCreate, realm: String = client.realm): R[Either[KeycloakError, ClientInitialAccess]] = {
    client.post[ClientInitialAccess](realm :: "clients-initial-access" :: Nil, config)
  }

  /** Retrieve all access tokens for the Realm. */
  def fetchInitialAccessTokens(realm: String = client.realm): R[Either[KeycloakError, Seq[ClientInitialAccess]]] = {
    client.get[Seq[ClientInitialAccess]](realm :: "clients-initial-access" :: Nil)
  }

  /** Delete an initial access token. */
  def deleteInitialAccessToken(tokenId: UUID, realm: String = client.realm): R[Either[KeycloakError, Unit]] = {
    client.delete[Unit](realm :: "clients-initial-access" :: tokenId.toString :: Nil)
  }
}

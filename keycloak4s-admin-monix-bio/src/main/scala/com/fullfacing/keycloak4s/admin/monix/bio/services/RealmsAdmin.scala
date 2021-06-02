package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class RealmsAdmin(implicit client: KeycloakClient) {

  /** Creates a realm. */
  def create(realm: Realm.Create): IO[KeycloakError, Unit] = {
    val path = Seq.empty[String]
    client.post[Unit](path, realm)
  }

  /** Retrieves the top-level representation of a realm. Does not include nested information like Users and Clients. */
  def fetch(realm: String = client.realm): IO[KeycloakError, Realm] = {
    val path = Seq(realm)
    client.get[Realm](path)
  }

  /** Retrieves the top-level representations of all realm. Does not include nested information like Users and Clients. */
  def fetchAll(): IO[KeycloakError, List[Realm]] = {
    val path = Seq()
    client.get[List[Realm]](path)
  }

  /** Composite of create and fetch. */
  def createAndRetrieve(realm: Realm.Create): IO[KeycloakError, Realm] = {
    create(realm).flatMap(_ => fetch(realm.realm))
  }

  /** Updates a realm. Ignores User, role or client information. */
  def update(update: Realm.Update, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm)
    client.put[Unit](path, update)
  }

  /** Deletes a realm. */
  def delete(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm)
    client.delete[Unit](path)
  }

  /** Retrieves a list of a realm's admin events. */
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
                       resourceTypes: Option[List[String]] = None): IO[KeycloakError, List[AdminEvent]] = {

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

  /** Deletes a realm's admin events. */
  def deleteAdminEvents(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "admin-events")
    client.delete[Unit](path)
  }

  /** Clears the cache of external public keys (Public keys of clients or Identity providers). */
  def clearKeysCache(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "clear-keys-cache")
    client.post[Unit](path)
  }

  /** Clears a realm's cache. */
  def clearRealmCache(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "clear-realm-cache")
    client.post[Unit](path)
  }

  /** Clears a user's cache. */
  def clearUserCache(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "clear-user-cache")
    client.post[Unit](path)
  }

  /* PURPOSE/USE CASES CURRENTLY UNKNOWN **/
  def clientDescriptionConverter(description: String, realm: String = client.realm): IO[KeycloakError, Client] = {
    val path = Seq(realm, "client-description-converter")
    client.post[Client](path, description)
  }

  /** Retrieves a realm's client session statistics. */
  def fetchClientSessionStats(realm: String = client.realm): IO[KeycloakError, Seq[ClientSessionStatistics]] = {
    val path = Seq(realm, "client-session-stats")
    client.get[Seq[ClientSessionStatistics]](path)
  }

  /** Retrieves a realm's default client scopes. */
  def fetchDefaultClientScopes(realm: String = client.realm): IO[KeycloakError, Seq[ClientScope]] = {
    val path = Seq(realm, "default-default-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /** Updates a realm's default client scope. */
  def assignClientScopeAsDefault(scopeId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId.toString)
    client.put[Unit](path)
  }

  /** Deletes a realm's default client scope. */
  def unassignClientScopeAsDefault(scopeId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-default-client-scopes", scopeId.toString)
    client.delete[Unit](path)
  }

  /** Retrieves a list of a realm's default groups. Only name, path and ids are returned.  */
  def fetchDefaultGroups(realm: String = client.realm): IO[KeycloakError, Seq[Group]] = {
    val path = Seq(realm, "default-groups")
    client.get[Seq[Group]](path)
  }

  /** Assigns a Group as default for a realm. */
  def assignGroupAsDefault(groupId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-groups", groupId.toString)
    client.put[Unit](path)
  }

  /** Unassigns a Group as default from a realm. */
  def unassignGroupAsDefault(groupId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-groups", groupId.toString)
    client.delete[Unit](path)
  }

  /** Retrieves a realm's optional client scopes. */
  def fetchOptionalClientScopes(realm: String = client.realm): IO[KeycloakError, Seq[ClientScope]] = {
    val path = Seq(realm, "default-optional-client-scopes")
    client.get[Seq[ClientScope]](path)
  }

  /** Updates a realm's optional client scope. */
  def assignClientScopeAsOptional(scopeId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-optional-client-scopes", scopeId.toString)
    client.put[Unit](path)
  }

  /** Deletes a realm's optional client scope. */
  def unassignClientScopeAsOptional(scopeId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "default-optional-client-scopes", scopeId.toString)
    client.delete[Unit](path)
  }

  /** Retrieves a list of a realm's events. */
  def fetchEvents(realm: String = client.realm,
                  clientName: Option[String] = None,
                  dateFrom: Option[String] = None,
                  dateTo: Option[String] = None,
                  first: Option[Int] = None,
                  ipAddress: Option[String] = None,
                  max: Option[Int] = None,
                  `type`: Option[List[String]] = None,
                  user: Option[String] = None): IO[KeycloakError, Seq[Event]] = {

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
    client.get[Seq[Event]](path, query = query)
  }

  /** Deletes a realm's events */
  def deleteAllEvents(realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "events")
    client.delete[Unit](path)
  }

  /** Get the events provider configuration. */
  def fetchEventsConfig(realm: String = client.realm): IO[KeycloakError, RealmEventsConfig] = {
    val path = Seq(realm, "events", "config")
    client.get[RealmEventsConfig](path)
  }

  /** Updates a realm's events provider. */
  def updateEventsConfig(config: RealmEventsConfig.Update, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "events", "config")
    client.put[Unit](path, config)
  }

  /** Retrieves a realm's group by its path. */
  def fetchGroupByPath(path: String, realm: String = client.realm): IO[KeycloakError, Group] = {
    client.get[Group](Seq(realm, "group-by-path", path))
  }

  /** Deletes a realmn's user sessions. */
  def logoutAll(realm: String = client.realm): IO[KeycloakError, GlobalRequestResult] = {
    val path = Seq(realm, "logout-all")
    client.post[GlobalRequestResult](path)
  }

  /** Partial exports a realm. */
  def partialExport(realm: String = client.realm,
                    exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): IO[KeycloakError, Realm] = {
    val path    = Seq(realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    client.post[Realm](path, query = queries)
  }

  /** Partially imports a realm. */
  def partialImport(rep: Realm.PartialImport, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "partialImport")
    client.post[Unit](path, rep)
  }

  /** Push the realm’s revocation policy to any client that has an admin url associated with it. */
  def pushRevocation(realm: String = client.realm): IO[KeycloakError, GlobalRequestResult] = {
    val path = Seq(realm, "push-revocation")
    client.post[GlobalRequestResult](path)
  }

  /** Deletes a specific user session from a realm. */
  def removeUserSession(session: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "sessions", session.toString)
    client.delete[Unit](path)
  }

  /** Tests a realm's LDAP connection. */
  def testLdapConnection(realm: String = client.realm,
                         action: Option[String] = None,
                         bindCredential: Option[String] = None,
                         bindDn: Option[String] = None,
                         componentId: Option[String] = None,
                         connectionTimeout: Option[String] = None,
                         connectionUrl: Option[String] = None,
                         useTruststoreSpi: Option[String] = None): IO[KeycloakError, Unit] = {

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

    // todo determine if endpoint requires application/json or multipart/form-data
    val mp = createMultipart(flattenOptionMap(queries))
    client.post[Unit](path, mp)
  }

  /** Tests a realm's SMTP connection. */
  def testSmtpConnection(config: String, realm: String = client.realm): IO[KeycloakError, Unit] = {
    val path = Seq(realm, "testSMTPConnection", config)
    client.post[Unit](path)
  }

  /** Retrieves a realm's user management permissions. */
  def fetchUsersManagementPermissions(realm: String = client.realm): IO[KeycloakError, ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    client.get[ManagementPermission](path)
  }

  /** Enables a realm's user management permissions. */
  def enableUsersManagementPermissions(realm: String = client.realm): IO[KeycloakError, ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(true))
  }

  /** Disables a realm's user management permissions. */
  def disableUsersManagementPermissions(realm: String = client.realm): IO[KeycloakError, ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(false))
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------ Client Registration Policies ------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves a list of a realm's client registration policy providers. */
  def fetchClientRegistrationPolicyProviders(realm: String = client.realm): IO[KeycloakError, List[ComponentType]] = {
    val path = Seq(realm, "client-registration-policy", "providers")
    client.get[List[ComponentType]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------- Initial Access Tokens ---------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Creates a new initial access token for a realm. */
  def createInitialAccessToken(config: ClientInitialAccess.Create, realm: String = client.realm): IO[KeycloakError, ClientInitialAccess] = {
    client.post[ClientInitialAccess](realm :: "clients-initial-access" :: Nil, config)
  }

  /** Retrieves a list of access tokens for a realm. */
  def fetchInitialAccessTokens(realm: String = client.realm): IO[KeycloakError, Seq[ClientInitialAccess]] = {
    client.get[Seq[ClientInitialAccess]](realm :: "clients-initial-access" :: Nil)
  }

  /** Deletes an initial access token from a realm. */
  def deleteInitialAccessToken(tokenId: UUID, realm: String = client.realm): IO[KeycloakError, Unit] = {
    client.delete[Unit](realm :: "clients-initial-access" :: tokenId.toString :: Nil)
  }
}

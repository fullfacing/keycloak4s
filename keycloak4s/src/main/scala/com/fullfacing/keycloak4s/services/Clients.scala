package com.fullfacing.keycloak4s.services

import java.io.File
import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.fullfacing.keycloak4s.models.enums.{InstallationProvider, InstallationProviders}

import scala.collection.immutable.Seq

class Clients[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Get client installation file */
  def getClientInstallationProvider(clientId: UUID, providerId: InstallationProvider = InstallationProviders.Json): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "installation", "providers", providerId.value)
    client.get[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  /**
   * Create a new client.
   * Client’s client_id must be unique!
   *
   * @param nClient  Object representing a Client's details.
   */
  def create(nClient: Client.Create): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients")
    client.post[Unit](path, nClient)
  }

  /**
   * Returns a list of clients belonging to the realm
   *
   * @param clientId      Optional clientId filter.
   * @param viewableOnly  Optional filter for clients that cannot be viewed in full by admin.
   * @return
   */
  def fetch(clientId: Option[String] = None, viewableOnly: Boolean = false): R[Either[KeycloakError, Seq[Client]]] = {
    val query = createQuery(("clientId", clientId), ("viewableOnly", Some(viewableOnly)))
    val path = Seq(client.realm, "clients")
    client.get[Seq[Client]](path, query = query)
  }

  /**
   * Get representation of a client.
   *
   * @param clientId  ID of client (not client-id).
   * @return
   */
  def fetchById(clientId: UUID): R[Either[KeycloakError, Client]] = {
    val path = Seq(client.realm, "clients", clientId.toString)
    client.get[Client](path)
  }

  /**
   * Update a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param c         Object representing a Client's details.
   */
  def update(clientId: UUID, c: Client.Update): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString)
    client.put[Unit](path, c)
  }

  /**
   * Deletes a client.
   *
   * @param clientId  ID of client (not client-id).
   * @return
   */
  def delete(clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString)
    client.delete[Unit](path)
  }

  def fetchServiceAccountUser(clientId: UUID): R[Either[KeycloakError, User]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "service-account-user")
    client.get[User](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ---------------------------------------------- Sessions ---------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Get application session count.
   * Returns a number of user sessions associated with this client { "count": number }.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def fetchUserSessionCount(clientId: UUID): R[Either[KeycloakError, Count]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "session-count")
    client.get[Count](path)
  }

  /**
   * Get user sessions for client. Returns a list of user sessions associated with this client.
   *
   * @param clientId ID of client (not client-id).
   * @param first    Optional paging offset.
   * @param max      Optional maximum results size (defaults to 100).
   */
  def fetchUserSessions(clientId: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[UserSession]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(client.realm, "clients", clientId.toString, "user-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  /**
   * Get application offline session count.
   * Returns a number of offline user sessions associated with this client { "count": number }.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def fetchOfflineSessionCount(clientId: UUID): R[Either[KeycloakError, Count]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "offline-session-count")
    client.get[Count](path)
  }

  /**
   * Get application offline sessions.
   * Returns a list of offline user sessions associated with this client.
   *
   * @param clientId ID of client (not client-id).
   * @param first    Optional paging offset.
   * @param max      Optional maximum results size (defaults to 100).
   * @return
   */
  def fetchOfflineSessions(clientId: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[UserSession]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(client.realm, "clients", clientId.toString, "offline-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Certificates -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Get key info.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @return
   */
  def getKeyInfo(attribute: String, clientId: UUID): R[Either[KeycloakError, Certificate]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute)
    client.get[Certificate](path)
  }

  /**
   * Get a keystore file for the client, containing private key and public certificate.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param config    Keystore configuration.
   * @return
   */
  def getKeystoreFile(attribute: String, clientId: UUID, config: KeyStoreConfig): R[Either[KeycloakError, File]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute, "download")
    client.post[File](path, config)
  }

  /**
   * Generate a new certificate with new key pair
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @return
   */
  def generateNewCertificate(attribute: String, clientId: UUID): R[Either[KeycloakError, Certificate]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute, "generate")
    client.post[Certificate](path)
  }

  /**
   * Generates a key pair and certificate and serves the private key in a specified keystore format.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param config    Keystore configuration.
   * @return
   */
  def generateAndDownloadNewCertificate(attribute: String, clientId: UUID, config: KeyStoreConfig): R[Either[KeycloakError, File]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute, "generate-and-download")
    client.post[File](path, config)
  }

  /**
   * Upload certificate and private key.
   *
   * @param attribute
   * @param clientId    ID of client (not client-id).
   * @param file
   * @return
   */
  def uploadCertificateWithPrivateKey(attribute: String, clientId: UUID, file: File): R[Either[KeycloakError, Certificate]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute, "upload")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  /**
   * Upload only certificate, not private key.
   *
   * @param attribute
   * @param clientId    ID of client (not client-id).
   * @param file
   * @return
   */
  def uploadCertificateWithoutPrivateKey(attribute: String, clientId: UUID, file: File): R[Either[KeycloakError, Certificate]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "certificates", attribute, "upload-certificate")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Client Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Create a new client scope. Client Scope’s name must be unique! */
  def createClientScope(clientScope: ClientScope.Create): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes")
    client.post[Unit](path, clientScope)
  }

  def fetchClientScopes(): R[Either[KeycloakError, List[ClientScope]]] = {
    val path = Seq(client.realm, "client-scopes")
    client.get[List[ClientScope]](path)
  }

  def fetchClientScopeById(scopeId: UUID): R[Either[KeycloakError, ClientScope]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString)
    client.get[ClientScope](path)
  }

  def updateClientScope(scopeId: UUID, clientScope: ClientScope.Update): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString)
    client.put[Unit](path, clientScope)
  }

  def deleteClientScope(scopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "client-scopes", scopeId.toString)
    client.delete[Unit](path)
  }

  /**  Get default client scopes. Only name and ids are returned. */
  def fetchDefaultClientScopes(clientId: UUID): R[Either[KeycloakError, List[ClientScope]]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "default-client-scopes")
    client.get[List[ClientScope]](path)
  }

  def updateDefaultClientScope(clientScopeId: String, clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "default-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  def removeDefaultClientScope(clientScopeId: String, clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "default-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  /**  Get optional client scopes. Only name and ids are returned. */
  def getOptionalClientScopes(clientId: UUID): R[Either[KeycloakError, List[ClientScope]]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "optional-client-scopes")
    client.get[List[ClientScope]](path)
  }

  def updateOptionalClientScope(clientScopeId: String, clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "optional-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  def deleteOptionalClientScope(clientScopeId: String, clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "optional-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Permissions -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def fetchClientAuthorisationPermissions(clientId: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Update client Authorization permissions. */
  def updateClientAuthorisationPermissions(clientId: UUID, permission: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "management", "permissions")
    client.put[ManagementPermission](path, permission)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------- Cluster Nodes -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Register a cluster node with the client.
   * Manually register cluster node to this client - usually it’s not needed to call this directly as adapter should
   * handle by sending registration request to Keycloak.
   *
   * @param clientId    ID of client (not client-id).
   * @param node
   * @return
   */
  def registerClusterNode(clientId: UUID, node: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "nodes")
    client.post[Unit](path, Map("node" -> node)) //If Sttp throws an error, then the node String needs to be wrapped in a case class instead of a Map.
  }

  /**
   * Unregister a cluster node from the client.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def unregisterClusterNode(clientId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "nodes")
    client.delete[Unit](path)
  }

  /**
   * Test if registered cluster nodes are available.
   * Tests availability by sending 'ping' request to all cluster nodes.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def testNodesAvailability(clientId: UUID): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "test-nodes-available")
    client.get[GlobalRequestResult](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Credentials --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generate a new secret for the client. */
  def regenerateClientSecret(clientId: UUID): R[Either[KeycloakError, Credential]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "client-secret")
    client.post[Credential](path)
  }

  /** Get the client secret. */
  def fetchClientSecret(clientId: UUID): R[Either[KeycloakError, Credential]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "client-secret")
    client.get[Credential](path)
  }

  /** Generate a new registration access token for the client. */
  def regenerateRegistrationAccessToken(clientId: UUID): R[Either[KeycloakError, Client]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "registration-access-token")
    client.post[Client](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------ Evaluate Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generate an example access token. */
  def generateAccessTokenExample(clientId: UUID, scope: Option[String] = None, userId: Option[String] = None): R[Either[KeycloakError, AccessToken]] = {
    val query = createQuery(("scope", scope), ("userId", userId))

    val path = Seq(client.realm, "clients", clientId.toString, "evaluate-scopes", "generate-example-access-token")
    client.get[AccessToken](path, query = query)
  }

  /** Return list of all protocol mappers, which will be used when generating tokens issued for particular client. */
  def getProtocolMappers(clientId: UUID, scope: Option[String] = None): R[Either[KeycloakError, Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(client.realm, "clients", clientId.toString, "evaluate-scopes", "protocol-mappers")
    client.get[Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]](path, query = query)
  }

  /**
   * Get effective scope mapping of all roles of particular role container, which this client is defacto allowed to have in the accessToken issued for him.
   * This contains scope mappings, which this client has directly, as well as scope mappings, which are granted to all client scopes, which are linked with this client.
   *
   * @param clientId        ID of client (not client-id).
   * @param roleContainerId Either realm name OR client UUID.
   * @param scope
   * @return
   */
  def getEffectiveScopeMapping(clientId: UUID, roleContainerId: String, scope: Option[String]): R[Either[KeycloakError, Seq[Role]]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(client.realm, "clients", clientId.toString, "evaluate-scopes", "scope-mappings", roleContainerId, "granted")
    client.get[Seq[Role]](path, query = query)
  }

  /**
   * Get roles, which this client doesn't have scope for and can't have them in the accessToken issued for him.
   *
   * @param clientId        ID of client (not client-id).
   * @param roleContainerId Either realm name OR client UUID.
   * @param scope
   * @return
   */
  def getNonScopeRoles(clientId: UUID, roleContainerId: String, scope: Option[String]): R[Either[KeycloakError, Seq[Role]]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(client.realm, "clients", clientId.toString, "evaluate-scopes", "scope-mappings", roleContainerId, "not-granted")
    client.get[Seq[Role]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Revocation --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Push the client’s revocation policy to its admin URL.
   * If the client has an admin URL, push revocation policy to it.
   *
   * @param clientId ID of client (not client-id).
   * @return
   */
  def pushRevocationPolicy(clientId: UUID): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path = Seq(client.realm, "clients", clientId.toString, "push-revocation")
    client.post[GlobalRequestResult](path)
  }
}
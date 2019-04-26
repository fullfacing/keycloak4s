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
  def getClientInstallationProvider(id: UUID, providerId: InstallationProvider = InstallationProviders.Json): R[Either[KeycloakError, InstallationConfig]] = {
    val path: Path = Seq(client.realm, "clients", id, "installation", "providers", providerId.value)
    client.get[InstallationConfig](path)
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
    val path: Path = Seq(client.realm, "clients")
    client.post[Unit](path, nClient)
  }

  /**
   * Returns a list of clients belonging to the realm
   *
   * @param clientId      Optional client ID filter.
   * @param viewableOnly  Optional filter for clients that cannot be viewed in full by admin.
   * @return
   */
  def fetch(clientId: Option[String] = None, viewableOnly: Boolean = false): R[Either[KeycloakError, Seq[Client]]] = {
    val query = createQuery(("clientId", clientId), ("viewableOnly", Some(viewableOnly)))
    val path: Path = Seq(client.realm, "clients")
    client.get[Seq[Client]](path, query = query)
  }

  /**
   * Get representation of a client.
   *
   * @param id  ID of client (not client-id).
   * @return
   */
  def fetchById(id: UUID): R[Either[KeycloakError, Client]] = {
    val path: Path = Seq(client.realm, "clients", id)
    client.get[Client](path)
  }

  /**
   * Update a client.
   *
   * @param id  ID of client (not client-id).
   * @param c   Object representing a Client's details.
   */
  def update(id: UUID, c: Client.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", id)
    client.put[Unit](path, c)
  }

  /**
   * Deletes a client.
   *
   * @param id  ID of client (not client-id).
   * @return
   */
  def delete(id: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", id)
    client.delete[Unit](path)
  }

  def fetchServiceAccountUser(id: UUID): R[Either[KeycloakError, User]] = {
    val path: Path = Seq(client.realm, "clients", id, "service-account-user")
    client.get[User](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ---------------------------------------------- Sessions ---------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Get application session count.
   * Returns a number of user sessions associated with this client { "count": number }.
   *
   * @param id ID of client (not client-id).
   * @return
   */
  def fetchUserSessionCount(id: UUID): R[Either[KeycloakError, Count]] = {
    val path: Path = Seq(client.realm, "clients", id, "session-count")
    client.get[Count](path)
  }

  /**
   * Get user sessions for client. Returns a list of user sessions associated with this client.
   *
   * @param id       ID of client (not client-id).
   * @param first    Optional paging offset.
   * @param max      Optional maximum results size (defaults to 100).
   */
  def fetchUserSessions(id: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[UserSession]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path: Path = Seq(client.realm, "clients", id, "user-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  /**
   * Get application offline session count.
   * Returns a number of offline user sessions associated with this client { "count": number }.
   *
   * @param id ID of client (not client-id).
   * @return
   */
  def fetchOfflineSessionCount(id: UUID): R[Either[KeycloakError, Count]] = {
    val path: Path = Seq(client.realm, "clients", id, "offline-session-count")
    client.get[Count](path)
  }

  /**
   * Get application offline sessions.
   * Returns a list of offline user sessions associated with this client.
   *
   * @param id       ID of client (not client-id).
   * @param first    Optional paging offset.
   * @param max      Optional maximum results size (defaults to 100).
   * @return
   */
  def fetchOfflineSessions(id: UUID, first: Option[Int] = None, max: Option[Int] = None): R[Either[KeycloakError, List[UserSession]]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path: Path = Seq(client.realm, "clients", id, "offline-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Certificates -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Get key info.
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @return
   */
  def getKeyInfo(attribute: String, id: UUID): R[Either[KeycloakError, Certificate]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute)
    client.get[Certificate](path)
  }

  /**
   * Get a keystore file for the client, containing private key and public certificate.
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @param config    Keystore configuration.
   * @return
   */
  def getKeystoreFile(attribute: String, id: UUID, config: KeyStoreConfig): R[Either[KeycloakError, File]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute, "download")
    client.post[File](path, config)
  }

  /**
   * Generate a new certificate with new key pair
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @return
   */
  def generateNewCertificate(attribute: String, id: UUID): R[Either[KeycloakError, Certificate]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute, "generate")
    client.post[Certificate](path)
  }

  /**
   * Generates a key pair and certificate and serves the private key in a specified keystore format.
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @param config    Keystore configuration.
   * @return
   */
  def generateAndDownloadNewCertificate(attribute: String, id: UUID, config: KeyStoreConfig): R[Either[KeycloakError, File]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute, "generate-and-download")
    client.post[File](path, config)
  }

  /**
   * Upload certificate and private key.
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @param file
   * @return
   */
  def uploadCertificateWithPrivateKey(attribute: String, id: UUID, file: File): R[Either[KeycloakError, Certificate]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute, "upload")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  /**
   * Upload only certificate, not private key.
   *
   * @param attribute
   * @param id        ID of client (not client-id).
   * @param file
   * @return
   */
  def uploadCertificateWithoutPrivateKey(attribute: String, id: UUID, file: File): R[Either[KeycloakError, Certificate]] = {
    val path: Path = Seq(client.realm, "clients", id, "certificates", attribute, "upload-certificate")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Client Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Create a new client scope. Client Scope’s name must be unique! */
  def createClientScope(clientScope: ClientScope.Create): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.post[Unit](path, clientScope)
  }

  def fetchClientScopes(): R[Either[KeycloakError, List[ClientScope]]] = {
    val path: Path = Seq(client.realm, "client-scopes")
    client.get[List[ClientScope]](path)
  }

  def fetchClientScopeById(scopeId: UUID): R[Either[KeycloakError, ClientScope]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.get[ClientScope](path)
  }

  def updateClientScope(scopeId: UUID, clientScope: ClientScope.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.put[Unit](path, clientScope)
  }

  def deleteClientScope(scopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "client-scopes", scopeId)
    client.delete[Unit](path)
  }

  /**  Get default client scopes. Only name and ids are returned. */
  def fetchDefaultClientScopes(clientId: UUID): R[Either[KeycloakError, List[ClientScope]]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes")
    client.get[List[ClientScope]](path)
  }

  def updateDefaultClientScope(clientId: UUID, clientScopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  def removeDefaultClientScope(clientId: UUID, clientScopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  /**  Get optional client scopes. Only name and ids are returned. */
  def getOptionalClientScopes(clientId: UUID): R[Either[KeycloakError, List[ClientScope]]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes")
    client.get[List[ClientScope]](path)
  }

  def updateOptionalClientScope(clientId: UUID, clientScopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  def deleteOptionalClientScope(clientId: UUID, clientScopeId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Permissions -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param id ID of client (not client-id).
   * @return
   */
  def fetchClientAuthorisationPermissions(id: UUID): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "clients", id, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Update client Authorization permissions. */
  def updateClientAuthorisationPermissions(id: UUID, permission: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "clients", id, "management", "permissions")
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
   * @param id    ID of client (not client-id).
   * @param node
   * @return
   */
  def registerClusterNode(id: UUID, node: String): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", id, "nodes")
    client.post[Unit](path, Map("node" -> node)) //If Sttp throws an error, then the node String needs to be wrapped in a case class instead of a Map.
  }

  /**
   * Unregister a cluster node from the client.
   *
   * @param id ID of client (not client-id).
   * @return
   */
  def unregisterClusterNode(id: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "clients", id, "nodes")
    client.delete[Unit](path)
  }

  /**
   * Test if registered cluster nodes are available.
   * Tests availability by sending 'ping' request to all cluster nodes.
   *
   * @param id ID of client (not client-id).
   * @return
   */
  def testNodesAvailability(id: UUID): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path: Path = Seq(client.realm, "clients", id, "test-nodes-available")
    client.get[GlobalRequestResult](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Credentials --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generate a new secret for the client. */
  def regenerateClientSecret(id: UUID): R[Either[KeycloakError, Credential]] = {
    val path: Path = Seq(client.realm, "clients", id, "client-secret")
    client.post[Credential](path)
  }

  /** Get the client secret. */
  def fetchClientSecret(id: UUID): R[Either[KeycloakError, Credential]] = {
    val path: Path = Seq(client.realm, "clients", id, "client-secret")
    client.get[Credential](path)
  }

  /** Generate a new registration access token for the client. */
  def regenerateRegistrationAccessToken(id: UUID): R[Either[KeycloakError, Client]] = {
    val path: Path = Seq(client.realm, "clients", id, "registration-access-token")
    client.post[Client](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------ Evaluate Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generate an example access token. */
  def generateAccessTokenExample(id: UUID, scope: Option[String] = None, userId: Option[String] = None): R[Either[KeycloakError, AccessToken]] = {
    val query = createQuery(("scope", scope), ("userId", userId))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "generate-example-access-token")
    client.get[AccessToken](path, query = query)
  }

  /** Return list of all protocol mappers, which will be used when generating tokens issued for particular client. */
  def getProtocolMappers(id: UUID, scope: Option[String] = None): R[Either[KeycloakError, Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "protocol-mappers")
    client.get[Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]](path, query = query)
  }

  /**
   * Get effective scope mapping of all roles of particular role container, which this client is defacto allowed to have in the accessToken issued for him.
   * This contains scope mappings, which this client has directly, as well as scope mappings, which are granted to all client scopes, which are linked with this client.
   *
   * @param id              ID of client (not client-id).
   * @param roleContainerId Either realm name OR client UUID.
   * @param scope
   * @return
   */
  def getEffectiveScopeMapping(id: UUID, roleContainerId: String, scope: Option[String]): R[Either[KeycloakError, Seq[Role]]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "scope-mappings", roleContainerId, "granted")
    client.get[Seq[Role]](path, query = query)
  }

  /**
   * Get roles, which this client doesn't have scope for and can't have them in the accessToken issued for him.
   *
   * @param id              ID of client (not client-id).
   * @param roleContainerId Either realm name OR client UUID.
   * @param scope
   * @return
   */
  def getNonScopeRoles(id: UUID, roleContainerId: String, scope: Option[String]): R[Either[KeycloakError, Seq[Role]]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "scope-mappings", roleContainerId, "not-granted")
    client.get[Seq[Role]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Revocation --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /**
   * Push the client’s revocation policy to its admin URL.
   * If the client has an admin URL, push revocation policy to it.
   */
  def pushRevocationPolicy(id: UUID): R[Either[KeycloakError, GlobalRequestResult]] = {
    val path: Path = Seq(client.realm, "clients", id, "push-revocation")
    client.post[GlobalRequestResult](path)
  }
}
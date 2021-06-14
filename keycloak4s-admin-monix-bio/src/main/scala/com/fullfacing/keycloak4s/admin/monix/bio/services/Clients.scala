package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.io.File
import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.enums.{InstallationProvider, InstallationProviders}
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class Clients(implicit client: KeycloakClient) {

  /** Retrieves a client's installation file. */
  def getInstallationProvider(clientId: UUID, providerId: InstallationProvider = InstallationProviders.Json): IO[KeycloakError, InstallationConfig] = {
    val path: Path = Seq(client.realm, "clients", clientId, "installation", "providers", providerId.value)
    client.get[InstallationConfig](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //

  /** Creates a new client, client_id must be unique. */
  def create(nClient: Client.Create): IO[KeycloakError, UUID] = {
    val path: Path = Seq(client.realm, "clients")
    client.post[Headers](path, nClient).map(extractUuid).flatMap(IO.fromEither)
  }

  /** Retrieves a list of clients. */
  def fetch(clientId: Option[String] = None, viewableOnly: Boolean = false): IO[KeycloakError, Seq[Client]] = {
    val query = createQuery(("clientId", clientId), ("viewableOnly", Some(viewableOnly)))
    val path: Path = Seq(client.realm, "clients")
    client.get[Seq[Client]](path, query = query)
  }

  /** Composite of create and fetch. */
  def createAndRetrieve(nClient: Client.Create): IO[KeycloakError, Client] = {
    create(nClient).flatMap(id => fetchById(id))
  }

  /** Retrieves a client by id. */
  def fetchById(clientId: UUID): IO[KeycloakError, Client] = {
    val path: Path = Seq(client.realm, "clients", clientId)
    client.get[Client](path)
  }

  /** Updates a client. */
  def update(clientId: UUID, c: Client.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId)
    client.put[Unit](path, c)
  }

  /** Deletes a client. */
  def delete(clientId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId)
    client.delete[Unit](path)
  }

  /** Retrieves the service account user of a client. */
  def fetchServiceAccountUser(clientId: UUID): IO[KeycloakError, User] = {
    val path: Path = Seq(client.realm, "clients", clientId, "service-account-user")
    client.get[User](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ---------------------------------------------- Sessions ---------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves the number of user sessions of a client. */
  def fetchUserSessionCount(clientId: UUID): IO[KeycloakError, Count] = {
    val path: Path = Seq(client.realm, "clients", clientId, "session-count")
    client.get[Count](path)
  }

  /** Retrieves a list of user sessions of a client. */
  def fetchUserSessions(clientId: UUID, first: Option[Int] = None, max: Option[Int] = None): IO[KeycloakError, List[UserSession]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path: Path = Seq(client.realm, "clients", clientId, "user-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  /** Retrieves the number of offline user sessions of a client. */
  def fetchOfflineSessionCount(clientId: UUID): IO[KeycloakError, Count] = {
    val path: Path = Seq(client.realm, "clients", clientId, "offline-session-count")
    client.get[Count](path)
  }

  /** Retrieves a list of offline user sessions of a client. */
  def fetchOfflineSessions(clientId: UUID, first: Option[Int] = None, max: Option[Int] = None): IO[KeycloakError, List[UserSession]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path: Path = Seq(client.realm, "clients", clientId, "offline-sessions")
    client.get[List[UserSession]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Certificates -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves information of a keystore for a certificate. */
  def fetchKeystoreInfo(certificateName: String, clientId: UUID): IO[KeycloakError, Certificate] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName)
    client.get[Certificate](path)
  }

  /** Retrieves a keystore file containing a private key and public certificate. */
  def fetchKeystoreFile(certificateName: String, clientId: UUID, config: KeyStoreConfig): IO[KeycloakError, File] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName, "download")
    client.post[File](path, config)
  }

  /** Generates a new certificate. */
  def generateNewCertificate(certificateName: String, clientId: UUID): IO[KeycloakError, Certificate] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName, "generate")
    client.post[Certificate](path)
  }

  /** Generates a new certificate and returns the private key in a specified keystore format. */
  def generateAndDownloadNewCertificate(certificateName: String, clientId: UUID, config: KeyStoreConfig): IO[KeycloakError, File] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName, "generate-and-download")
    client.post[File](path, config)
  }

  /** Uploads a certificate with a private key. */
  def uploadCertificateWithPrivateKey(certificateName: String, clientId: UUID, file: File): IO[KeycloakError, Certificate] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName, "upload")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  /** Uploads a certificate without a private key. */
  def uploadCertificateWithoutPrivateKey(certificateName: String, clientId: UUID, file: File): IO[KeycloakError, Certificate] = {
    val path: Path = Seq(client.realm, "clients", clientId, "certificates", certificateName, "upload-certificate")
    val multipart = createMultipart(file)
    client.post[Certificate](path, multipart)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ----------------------------------------- Role Scope Mapping ----------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves roles mapped to a client. */
  def fetchMappedRoles(clientId: UUID): IO[KeycloakError, Mappings] = {
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings")
    client.get[Mappings](path)
  }

  /** Maps realm roles to a client. */
  def addRealmRoles(clientId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings", "realm")
    client.post[Unit](path, body)
  }

  /** Unmaps realm roles from a client. */
  def removeRealmRoles(clientId: UUID, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
    val body = roleIds.map(Role.Id)
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings", "realm")
    client.delete[Unit](path, body)
  }

  /** Retrieves realm roles mapped to a client. */
  def fetchMappedRealmRoles(clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings", "realm")
    client.get[List[Role]](path)
  }

  /** Retrieves available realm roles for to a client. */
  def fetchAvailableRealmRoles(clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /** Retrieves effective realm roles of a client. */
  def fetchEffectiveRealmRoles(clientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "scope-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }

  /** Maps client roles to a client. */
  def addClientRoles(targetClientId: UUID, sourceClientId: UUID, roleNames: List[String]): IO[KeycloakError, Unit] = {
    val body = roleNames.map(Role.Name)
    val path: Path = Seq(client.realm, "clients", targetClientId, "scope-mappings", "clients", sourceClientId)
    client.post[Unit](path, body)
  }

  /** Unmaps client roles from a client. */
  def removeClientRoles(targetClientId: UUID, sourceClientId: UUID, roleNames: List[String]): IO[KeycloakError, Unit] = {
    val body = roleNames.map(Role.Name)
    val path: Path = Seq(client.realm, "clients", targetClientId, "scope-mappings", "clients", sourceClientId)
    client.delete[Unit](path, body)
  }

  /** Retrieves client roles mapped to a client. */
  def fetchMappedClientRoles(targetClientId: UUID, sourceClientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", targetClientId, "scope-mappings", "clients", sourceClientId)
    client.get[List[Role]](path)
  }

  /** Retrieves available client roles for to a client. */
  def fetchAvailableClientRoles(targetClientId: UUID, sourceClientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", targetClientId, "scope-mappings", "clients", sourceClientId, "available")
    client.get[List[Role]](path)
  }

  /** Retrieves effective client roles of a client. */
  def fetchEffectiveClientRoles(targetClientId: UUID, sourceClientId: UUID): IO[KeycloakError, List[Role]] = {
    val path: Path = Seq(client.realm, "clients", targetClientId, "scope-mappings", "clients", sourceClientId, "composite")
    client.get[List[Role]](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Client Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves default client scopes for a client. */
  def fetchDefaultClientScopes(clientId: UUID): IO[KeycloakError, List[ClientScope]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes")
    client.get[List[ClientScope]](path)
  }

  /** Updates a default client scope for a client. */
  def updateDefaultClientScope(clientId: UUID, clientScopeId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  /** Deletes a default client scope from a client. */
  def deleteDefaultClientScope(clientId: UUID, clientScopeId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "default-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  /** Retrieves optional client scopes for a client. */
  def fetchOptionalClientScopes(clientId: UUID): IO[KeycloakError, List[ClientScope]] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes")
    client.get[List[ClientScope]](path)
  }

  /** Updates an optional client scope for a client. */
  def updateOptionalClientScope(clientId: UUID, clientScopeId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    client.put[Unit](path)
  }

  /** Deletes an optional client scope from a client. */
  def deleteOptionalClientScope(clientId: UUID, clientScopeId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    client.delete[Unit](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Permissions -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Retrieves details of a client's management permissions. */
  def fetchManagementPermissions(clientId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "clients", clientId, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Enables a client's management permissions. */
  def enableManagementPermissions(clientId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "clients", clientId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(true))
  }

  /** Disables a client's management permissions. */
  def disableManagementPermissions(clientId: UUID): IO[KeycloakError, ManagementPermission] = {
    val path: Path = Seq(client.realm, "clients", clientId, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(false))
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------- Cluster Nodes -------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Registers a cluster node with the client. */
  def registerClusterNode(clientId: UUID, node: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "nodes")
    client.post[Unit](path, Map("node" -> node))
  }

  /**  Unregisters a cluster node from the client. */
  def unregisterClusterNode(clientId: UUID, node: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "clients", clientId, "nodes", node)
    client.delete[Unit](path)
  }

  /** Test if registered cluster nodes are available. */
  def testNodesAvailability(clientId: UUID): IO[KeycloakError, GlobalRequestResult] = {
    val path: Path = Seq(client.realm, "clients", clientId, "test-nodes-available")
    client.get[GlobalRequestResult](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------- Credentials --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generates a new secret for the client. */
  def regenerateClientSecret(clientId: UUID): IO[KeycloakError, Credential] = {
    val path: Path = Seq(client.realm, "clients", clientId, "client-secret")
    client.post[Credential](path)
  }

  /** Retrieves the client secret. */
  def fetchClientSecret(clientId: UUID): IO[KeycloakError, Credential] = {
    val path: Path = Seq(client.realm, "clients", clientId, "client-secret")
    client.get[Credential](path)
  }

  /** Generates a new registration access token for the client. */
  def regenerateRegistrationAccessToken(clientId: UUID): IO[KeycloakError, Client] = {
    val path: Path = Seq(client.realm, "clients", clientId, "registration-access-token")
    client.post[Client](path)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------ Evaluate Scopes ------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Generates an example access token. */
  def generateAccessTokenExample(id: UUID, scope: Option[String] = None, userId: Option[String] = None): IO[KeycloakError, AccessToken] = {
    val query = createQuery(("scope", scope), ("userId", userId))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "generate-example-access-token")
    client.get[AccessToken](path, query = query)
  }

  /** Retrieves a list of all protocol mappers, which will be used when generating tokens issued for particular client. */
  def fetchProtocolMappers(id: UUID, scope: Option[String] = None): IO[KeycloakError, Seq[ProtocolMapperEvaluation]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "protocol-mappers")
    client.get[Seq[ProtocolMapperEvaluation]](path, query = query)
  }

  /**
   * Retrieves a list of effective scope mapping of all roles for a particular role container (either a realm name or a client UUID). */
  def fetchEffectiveScopeMapping(id: UUID, roleContainer: String, scope: Option[String]): IO[KeycloakError, Seq[Role]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "scope-mappings", roleContainer, "granted")
    client.get[Seq[Role]](path, query = query)
  }

  /** Retrieves a list of roles outside of the scope of a client. */
  def fetchNonScopeRoles(id: UUID, roleContainer: String, scope: Option[String]): IO[KeycloakError, Seq[Role]] = {
    val query = createQuery(("scope", scope))

    val path: Path = Seq(client.realm, "clients", id, "evaluate-scopes", "scope-mappings", roleContainer, "not-granted")
    client.get[Seq[Role]](path, query = query)
  }

  // ------------------------------------------------------------------------------------------------------ //
  // --------------------------------------------- Revocation --------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------ //

  /** Push a client’s revocation policy to its admin URL. */
  def pushRevocationPolicy(clientId: UUID): IO[KeycloakError, GlobalRequestResult] = {
    val path: Path = Seq(client.realm, "clients", clientId, "push-revocation")
    client.post[GlobalRequestResult](path)
  }
}
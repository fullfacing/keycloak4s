package com.fullfacing.keycloak4s.services

import java.io.File

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Multipart

import scala.collection.immutable.Seq

class Clients[R[_]: Concurrent, S](implicit keycloakClient: KeycloakClient[R, S]) {

  /**
   * Create a new initial access token.
   *
   * @param realm   Name of the Realm.
   * @param config
   * @return
   */
  def createNewInitialAccessToken(realm: String, config: ClientInitialAccessCreate): R[ClientInitialAccess] = {
    keycloakClient.post[ClientInitialAccessCreate, ClientInitialAccess](realm :: "clients-initial-access" :: Nil, config)
  }

  /**
    * Retrieve all access tokens for the Realm.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def getInitialAccessTokens(realm: String): R[Seq[ClientInitialAccess]] = {
    keycloakClient.get[Seq[ClientInitialAccess]](realm :: "clients-initial-access" :: Nil)
  }

  /**
    * Delete an initial access token.
    *
    * @param realm   Name of the Realm.
    * @return
    */
  def deleteInitialAccessToken(tokenId: String, realm: String): R[Unit] = {
    keycloakClient.delete(realm :: "clients-initial-access" :: tokenId :: Nil)
  }


    /**
     * Create a new client.
     * Client’s client_id must be unique!
     *
     * @param realm   Name of the Realm.
     * @param client  Object representing a Client's details.
     * @return
     */
    def createNewClient(realm: String, client: Client): R[Unit] = {
      val path = Seq(realm, "clients")
      keycloakClient.post[Client, Unit](path, client)
    }

    /**
     * Returns a list of clients belonging to the realm
     *
     * @param realm         Name of the Realm.
     * @param clientId      Optional clientId filter.
     * @param viewableOnly  Optional filter for clients that cannot be viewed in full by admin.
     * @return
     */
    def getRealmClients(realm: String, clientId: Option[String] = None, viewableOnly: Boolean = false): R[Seq[Client]] = {
      val query = createQuery(("clientId", clientId), ("viewableOnly", Some(viewableOnly)))
      val path = Seq(realm, "clients")
      keycloakClient.get[Seq[Client]](path, query = query)
    }

    /**
     * Get representation of a client.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def getClient(clientId: String, realm: String): R[Client] = {
      val path = Seq(realm, "clients", clientId)
      keycloakClient.get[Client](path)
    }

    /**
     * Update a client.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @param c         Object representing a Client's details.
     * @return
     */
    def updateClient(clientId: String, realm: String, c: Client): R[Unit] = {
      val path = Seq(realm, "clients", clientId)
      keycloakClient.put[Client, Unit](path, c)
    }

    /**
     * Deletes a client.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def deleteClient(clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId)
      keycloakClient.delete(path)
    }

    /**
     * Generate a new secret for the client.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def generateClientSecret(clientId: String, realm: String): R[Credential] = {
      val path = Seq(realm, "clients", clientId, "client-secret")
      keycloakClient.post[Unit, Credential](path)
    }

    /**
     * Get the client secret.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def getClientSecret(clientId: String, realm: String): R[Credential] = {
      val path = Seq(realm, "clients", clientId, "client-secret")
      keycloakClient.get[Credential](path)
    }

    /**
     * Get default client scopes.
     * Only name and ids are returned.
     *
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def getDefaultClientScopes(clientId: String, realm: String): R[List[ClientScope]] = {
      val path = Seq(realm, "clients", clientId, "default-client-scopes")
      keycloakClient.get[List[ClientScope]](path)
    }

    /**
     * ??? //TODO Determine route functionality.
     *
     * @param clientScopeId
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @return
     */
    def updateClientScope(clientScopeId: String, clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "default-client-scopes", clientScopeId)
      keycloakClient.put(path)
    }

    /**
     * Deletes a client scope.
     *
     * @param clientScopeId
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @return
     */
    def deleteClientScope(clientScopeId: String, clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "default-client-scopes", clientScopeId)
      keycloakClient.delete(path)
    }

    /**
     * Generate an example access token.
     *
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @param scope
     * @param userId
     * @return
     */
    def generateAccessTokenExample(clientId: String, realm: String, scope: Option[String] = None, userId: Option[String] = None): R[AccessToken] = {
      val query = createQuery(
        ("scope", scope),
        ("userId", userId)
      )

      val path = Seq(realm, "clients", clientId, "evaluate-scopes", "generate-example-access-token")
      keycloakClient.get[AccessToken](path, query = query)
    }

    /**
     * Return list of all protocol mappers, which will be used when generating tokens issued for particular client.
     *
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @param scope
     * @return
     */
    def getProtocolMappers(clientId: String, realm: String, scope: Option[String] = None): R[Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]] = {
      val query = createQuery(("scope", scope))

      val path = Seq(realm, "clients", clientId, "evaluate-scopes", "protocol-mappers")
      keycloakClient.get(path, query = query)
    }

    /**
     * Get effective scope mapping of all roles of particular role container, which this client is defacto allowed to have in the accessToken issued for him.
     * This contains scope mappings, which this client has directly, as well as scope mappings, which are granted to all client scopes, which are linked with this client.
     *
     * @param clientId        ID of client (not client-id).
     * @param realm           Name of the Realm.
     * @param roleContainerId Either realm name OR client UUID.
     * @param scope
     * @return
     */
    def getEffectiveScopeMapping(clientId: String, realm: String, roleContainerId: String, scope: Option[String]): R[Seq[Role]] = {
      val query = createQuery(("scope", scope))

      val path = Seq(realm, "clients", clientId, "evaluate-scopes", "scope-mappings", roleContainerId, "granted")
      keycloakClient.get(path, query = query)
    }

    /**
     * Get roles, which this client doesn't have scope for and can't have them in the accessToken issued for him.
     *
     * @param clientId        ID of client (not client-id).
     * @param realm           Name of the Realm.
     * @param roleContainerId Either realm name OR client UUID.
     * @param scope
     * @return
     */
    def getNonScopeRoles(clientId: String, realm: String, roleContainerId: String, scope: Option[String]): R[Seq[Role]] = {
      val query = createQuery(("scope", scope))

      val path = Seq(realm, "clients", clientId, "evaluate-scopes", "scope-mappings", roleContainerId, "not-granted")
      keycloakClient.get(path, query = query)
    }

    /**
     * ???
     *
     * @param clientId    ID of client (not client-id).
     * @param providerId  ID of provider.
     * @param realm       Name of the Realm.
     * @return
     */
    def getClientInstallationProvider(clientId: String, providerId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "installation", "providers", providerId)
      keycloakClient.get(path)
    }

    /**
     * Return object stating whether client Authorization permissions have been initialized or not and a reference.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def getClientAuthorizationPermissions(clientId: String, realm: String): R[ManagementPermission] = {
      val path = Seq(realm, "clients", clientId, "management", "permissions")
      keycloakClient.get[ManagementPermission](path)
    }

    /**
     * Update client Authorization permissions.
     *
     * @param clientId    ID of client (not client-id).
     * @param realm       Name of the Realm.
     * @param permission
     * @return
     */
    def updateClientAuthorizationPermissions(clientId: String, realm: String, permission: ManagementPermission): R[ManagementPermission] = {
      val path = Seq(realm, "clients", clientId, "management", "permissions")
      keycloakClient.put[ManagementPermission, ManagementPermission](path, permission)
    }

    /**
     * Register a cluster node with the client.
     * Manually register cluster node to this client - usually it’s not needed to call this directly as adapter should handle by sending registration request to Keycloak.
     *
     * @param clientId    ID of client (not client-id).
     * @param realm       Name of the Realm.
     * @param node
     * @return
     */
    def registerClusterNode(clientId: String, realm: String, node: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "nodes")
      keycloakClient.post[Map[String, String], Unit](path, Map("node" -> node)) //If Sttp throws an error, then the node String needs to be wraooed in a case class instead of a Map.
    }

    /**
     * Unregister a cluster node from the client.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def unregisterClusterNode(clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "nodes")
      keycloakClient.delete(path)
    }

    /**
     * Get application offline session count.
     * Returns a number of offline user sessions associated with this client { "count": number }.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def getOfflineSessionCount(clientId: String, realm: String): R[Count] = {
      val path = Seq(realm, "clients", clientId, "offline-session-count")
      keycloakClient.get[Count](path)
    }

    /**
     * Get application offline sessions.
     * Returns a list of offline user sessions associated with this client.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @param first    Optional paging offset.
     * @param max      Optional maximum results size (defaults to 100).
     * @return
     */
    def getOfflineSessions(clientId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None): R[List[UserSession]] = {
      val query = createQuery(
        ("first", first),
        ("max", max)
      )

      val path = Seq(realm, "clients", clientId, "offline-sessions")
      keycloakClient.get[List[UserSession]](path, query = query)
    }

    /**
     * Returns optional client scopes.
     * Only name and ids are returned.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def getOptionalClientScopes(clientId: String, realm: String): R[List[ClientScope]] = {
      val path = Seq(realm, "clients", clientId, "optional-client-scopes")
      keycloakClient.get[List[ClientScope]](path)
    }

    /**
     * ??? //TODO Determine route functionality.
     *
     * @param clientScopeId
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @return
     */
    def updateOptionalClientScope(clientScopeId: String, clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "optional-client-scopes", clientScopeId)
      keycloakClient.put(path)
    }

    /**
     * Deletes an optional client scope.
     *
     * @param clientScopeId
     * @param clientId      ID of client (not client-id).
     * @param realm         Name of the Realm.
     * @return
     */
    def deleteOptionalClientScope(clientScopeId: String, clientId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "clients", clientId, "optional-client-scopes", clientScopeId)
      keycloakClient.delete(path)
    }

    /**
     * Push the client’s revocation policy to its admin URL.
     * If the client has an admin URL, push revocation policy to it.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def pushRevocationPolicy(clientId: String, realm: String): R[GlobalRequestResult] = {
      val path = Seq(realm, "clients", clientId, "push-revocation")
      keycloakClient.post[Unit, GlobalRequestResult](path)
    }

    /**
     * Generate a new registration access token for the client.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def generateRegistrationAccessToken(clientId: String, realm: String): R[Client] = {
      val path = Seq(realm, "clients", clientId, "registration-access-token")
      keycloakClient.post[Unit, Client](path)
    }

    /**
     * Get a user dedicated to the service account.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def getServiceAccountUser(clientId: String, realm: String): R[User] = {
      val path = Seq(realm, "clients", clientId, "service-account-user")
      keycloakClient.get[User](path)
    }

    /**
     * Get application session count.
     * Returns a number of user sessions associated with this client { "count": number }.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def getSessionCount(clientId: String, realm: String): R[Count] = {
      val path = Seq(realm, "clients", clientId, "session-count")
      keycloakClient.get[Count](path)
    }

    /**
     * Test if registered cluster nodes are available.
     * Tests availability by sending 'ping' request to all cluster nodes.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @return
     */
    def testNodesAvailability(clientId: String, realm: String): R[GlobalRequestResult] = {
      val path = Seq(realm, "clients", clientId, "test-nodes-available")
      keycloakClient.get[GlobalRequestResult](path)
    }

    /**
     * Get user sessions for client.
     * Returns a list of user sessions associated with this client.
     *
     * @param clientId ID of client (not client-id).
     * @param realm    Name of the Realm.
     * @param first    Optional paging offset.
     * @param max      Optional maximum results size (defaults to 100).
     * @return
     */
    def getUserSessions(clientId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None): R[List[UserSession]] = {
      val query = createQuery(
        ("first", first),
        ("max", max)
      )

      val path = Seq(realm, "clients", clientId, "user-sessions")
      keycloakClient.get[List[UserSession]](path, query = query)
    }

    /**
     * Base path for retrieving providers with the configProperties properly filled.
     *
     * @param realm Name of the Realm.
     * @return
     */
    def getClientRegistrationPolicyProviders(realm: String): R[List[ComponentType]] = {
      val path = Seq(realm, "client-registration-policy", "providers")
      keycloakClient.get[List[ComponentType]](path)
    }

    /**
     * Add client-level roles to the group role mapping.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @param roles
     * @return
     */
    def addRolesToGroup(clientId: String, groupId: String, realm: String, roles: Seq[Role]): R[Unit] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId)
      keycloakClient.post(path, roles)
    }

    /**
     * Get client-level role mappings for the group.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @return
     */
    def getGroupRoleMappings(clientId: String, groupId: String, realm: String): R[Seq[Role]] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId)
      keycloakClient.get(path)
    }

    /**
     * Delete client-level roles from group role mapping.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @param roles
     * @return
     */
    def deleteGroupRoles(clientId: String, groupId: String, realm: String, roles: Seq[Role]): R[Unit] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId)
      keycloakClient.delete(path, roles)
    }

    /**
     * Get available client-level roles that can be mapped to the group.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @return
     */
    def getAvailableGroupRoles(clientId: String, groupId: String, realm: String): R[List[Role]] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId, "available")
      keycloakClient.get[List[Role]](path)
    }

    /**
     * Get effective client-level group role mappings.
     * This recurses any composite roles.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @return
     */
    def getEffectiveGroupRoles(clientId: String, groupId: String, realm: String): R[List[Role]] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId, "composite")
      keycloakClient.get[List[Role]](path)
    }

    /**
     * Add client-level roles to the user role mapping.
     *
     * @param clientId
     * @param userId
     * @param realm   Name of the Realm.
     * @param roles
     * @return
     */
    def addRolesToUser(clientId: String, userId: String, realm: String, roles: Seq[Role]): R[Unit] = {
      val path = Seq(realm, "users", userId, "role-mappings", "clients", clientId)
      keycloakClient.post(path, roles)
    }

    /**
     * Get client-level role mappings for the user.
     *
     * @param clientId
     * @param userId
     * @param realm   Name of the Realm.
     * @return
     */
    def getUserRoleMappings(clientId: String, userId: String, realm: String): R[List[Role]] = {
      val path = Seq(realm, "users", userId, "role-mappings", "clients", clientId)
      keycloakClient.get[List[Role]](path)
    }

    /**
     * Delete client-level roles from user role mapping.
     *
     * @param clientId
     * @param groupId
     * @param realm   Name of the Realm.
     * @param roles
     * @return
     */
    def deleteUserRoles(clientId: String, groupId: String, realm: String, roles: Seq[Role]): R[Unit] = {
      val path = Seq(realm, "groups", groupId, "role-mappings", "clients", clientId)
      keycloakClient.delete(path, roles)
    }

    /**
     * Get available client-level roles that can be mapped to the user.
     *
     * @param clientId
     * @param userId
     * @param realm   Name of the Realm.
     * @return
     */
    def getAvailableUserRoles(clientId: String, userId: String, realm: String): R[List[Role]] = {
      val path = Seq(realm, "users", userId, "role-mappings", "clients", clientId, "available")
      keycloakClient.get[List[Role]](path)
    }

    /**
     * Get effective client-level user role mappings.
     * This recurses any composite roles.
     *
     * @param clientId
     * @param userId
     * @param realm   Name of the Realm.
     * @return
     */
    def getEffectiveUserRoles(clientId: String, userId: String, realm: String): R[List[Role]] = {
      val path = Seq(realm, "users", userId, "role-mappings", "clients", clientId, "composite")
      keycloakClient.get[List[Role]](path)
    }

    /**
     * Get key info.
     *
     * @param attribute
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def getKeyInfo(attribute: String, clientId: String, realm: String): R[Certificate] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute)
      keycloakClient.get[Certificate](path)
    }

    /**
     * Get a keystore file for the client, containing private key and public certificate.
     *
     * @param attribute
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @param config    Keystore configuration.
     * @return
     */
    def getKeystoreFile(attribute: String, clientId: String, realm: String, config: KeyStoreConfig): R[File] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute, "download")
      keycloakClient.post[KeyStoreConfig, File](path, config)
    }

    /**
     * Generate a new certificate with new key pair
     *
     * @param attribute
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @return
     */
    def generateNewCertificate(attribute: String, clientId: String, realm: String): R[Certificate] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute, "generate")
      keycloakClient.post[Unit, Certificate](path)
    }

    /**
     * Generates a keypair and certificate and serves the private key in a specified keystore format.
     *
     * @param attribute
     * @param clientId  ID of client (not client-id).
     * @param realm     Name of the Realm.
     * @param config    Keystore configuration.
     * @return
     */
    def generateAndDownloadNewCertificate(attribute: String, clientId: String, realm: String, config: KeyStoreConfig): R[File] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute, "generate-and-download")
      keycloakClient.post[KeyStoreConfig, File](path, config)
    }

    /**
     * Upload certificate and private key.
     *
     * @param attribute
     * @param clientId    ID of client (not client-id).
     * @param realm       Name of the Realm.
     * @param file
     * @return
     */
    def uploadCertificateWithPrivateKey(attribute: String, clientId: String, realm: String, file: File): R[Certificate] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute, "upload")
      val multipart = createMultipart(file)
      keycloakClient.post[Multipart, Certificate](path, multipart)
    }

    /**
     * Upload only certificate, not private key.
     *
     * @param attribute
     * @param clientId    ID of client (not client-id).
     * @param realm       Name of the Realm.
     * @param file
     * @return
     */
    def uploadCertificateWithoutPrivateKey(attribute: String, clientId: String, realm: String, file: File): R[Certificate] = {
      val path = Seq(realm, "clients", clientId, "certificates", attribute, "upload-certificate")
      val multipart = createMultipart(file)
      keycloakClient.post[Multipart, Certificate](path, multipart)
    }

    /**
     * Create a new client scope.
     * Client Scope’s name must be unique!
     *
     * @param realm       Name of the Realm.
     * @param clientScope Object representing ClientScope details.
     * @return
     */
    def createNewClientScope(realm: String, clientScope: ClientScope): R[Unit] = {
      val path = Seq(realm, "client-scopes")
      keycloakClient.post[ClientScope, Unit](path, clientScope)
    }

    /**
     * Update a client scope.
     *
     * @param scopeId     ID of the ClientScope.
     * @param realm       Name of the Realm.
     * @param clientScope Object representing ClientScope details.
     * @return
     */
    def updateClientScope(scopeId: String, realm: String, clientScope: ClientScope): R[Unit] = {
      val path = Seq(realm, "client-scopes", scopeId)
      keycloakClient.put[ClientScope, Unit](path, clientScope)
    }

    /**
     * Delete a client scope.
     *
     * @param scopeId ID of the ClientScope.
     * @param realm   Name of the Realm.
     * @return
     */
    def deleteClientScope(scopeId: String, realm: String): R[Unit] = {
      val path = Seq(realm, "client-scopes", scopeId)
      keycloakClient.delete(path)
    }

    /**
     * Returns a list of client scopes belonging to the realm.
     *
     * @param realm Name of the Realm.
     * @return
     */
    def getRealmClientScopes(realm: String): R[List[ClientScope]] = {
      val path = Seq(realm, "client-scopes")
      keycloakClient.get[List[ClientScope]](path)
    }

    /**
     * Get representation of the client scope.
     *
     * @param scopeId ID of the ClientScope.
     * @param realm   Name of the Realm.
     * @return
     */
    def getClientScope(scopeId: String, realm: String): R[ClientScope] = {
      val path = Seq(realm, "client-scopes", scopeId)
      keycloakClient.get(path)
    }
}
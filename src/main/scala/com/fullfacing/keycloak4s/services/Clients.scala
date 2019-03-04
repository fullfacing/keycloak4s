package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.handles.SttpClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object Clients {

  /**
   * Create a new client.
   * Client’s client_id must be unique!
   *
   * @param realm   Name of the Realm.
   * @param client  Object representing a Client's details.
   * @return
   */
  def createNewClient(realm: String, client: Client)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "clients")
    SttpClient.post(client, path)
  }

  /**
   * Returns a list of clients belonging to the realm
   *
   * @param realm         Name of the Realm.
   * @param clientId      Optional clientId filter.
   * @param viewableOnly  Optional filter for clients that cannot be viewed in full by admin.
   * @return
   */
  def getRealmClients(realm: String, clientId: Option[String] = None, viewableOnly: Boolean = false)(implicit authToken: String): AsyncApolloResponse[Seq[Client]] = {
    val query = createQuery(
      ("clientId", clientId),
      ("viewableOnly", Some(viewableOnly))
    )

    val path = Seq(realm, "clients")
    SttpClient.get(path, query)
  }

  /**
   * Get representation of a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def getClient(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Client] = {
    val path = Seq(realm, "clients", clientId)
    SttpClient.get(path)
  }

  /**
   * Update a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param client    Object representing a Client's details.
   * @return
   */
  def updateClient(clientId: String, realm: String, client: Client)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "clients", clientId)
    SttpClient.put(client, path)
  }

  /**
   * Deletes a client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteClient(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId)
    SttpClient.delete(path)
  }

  /**
   * Generate a new secret for the client.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def generateClientSecret(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Credential] = {
    val path = Seq(realm, "clients", clientId, "client-secret")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Get the client secret.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def getClientSecret(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Credential] = {
    val path = Seq(realm, "clients", clientId, "client-secret")
    SttpClient.get(path)
  }

  /**
   * Get default client scopes.
   * Only name and ids are returned.
   *
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def getDefaultClientScopes(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ClientScope]] = {
    val path = Seq(realm, "clients", clientId, "default-client-scopes")
    SttpClient.get(path)
  }

  /**
   * ??? //TODO Determine route functionality.
   *
   * @param clientScopeId
   * @param clientId      ID of client (not client-id).
   * @param realm         Name of the Realm.
   * @return
   */
  def updateClientScope(clientScopeId: String, clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId, "default-client-scopes", clientScopeId)
    SttpClient.put(path, Seq.empty[KeyValue])
  }

  /**
   * Deletes a client scope.
   *
   * @param clientScopeId
   * @param clientId      ID of client (not client-id).
   * @param realm         Name of the Realm.
   * @return
   */
  def deleteClientScope(clientScopeId: String, clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId, "default-client-scopes", clientScopeId)
    SttpClient.delete(path, Seq.empty[KeyValue])
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
  def generateAccessTokenExample(clientId: String, realm: String, scope: Option[String] = None, userId: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[AccessToken] = {
    val query = createQuery(
      ("scope", scope),
      ("userId", userId)
    )

    val path = Seq(realm, "clients", clientId, "evaluate-scopes", "generate-example-access-token")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Return list of all protocol mappers, which will be used when generating tokens issued for particular client.
   *
   * @param clientId      ID of client (not client-id).
   * @param realm         Name of the Realm.
   * @param scope
   * @return
   */
  def getProtocolMappers(clientId: String, realm: String, scope: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[Seq[ClientScopeEvaluateResourceProtocolMapperEvaluation]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(realm, "clients", clientId, "evaluate-scopes", "protocol-mappers")
    SttpClient.get(path, query.to[Seq])
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
  def getEffectiveScopeMapping(clientId: String, realm: String, roleContainerId: String, scope: Option[String])(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(realm, "clients", clientId, "evaluate-scopes", "scope-mappings", roleContainerId, "granted")
    SttpClient.get(path, query.to[Seq])
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
  def getNonScopeRoles(clientId: String, realm: String, roleContainerId: String, scope: Option[String])(implicit authToken: String): AsyncApolloResponse[Seq[Role]] = {
    val query = createQuery(("scope", scope))

    val path = Seq(realm, "clients", clientId, "evaluate-scopes", "scope-mappings", roleContainerId, "not-granted")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Returns an installation provider.
   *
   * @param clientId    ID of client (not client-id).
   * @param providerId  ID of provider.
   * @param realm       Name of the Realm.
   * @return
   */
  def getClientInstallationProvider(clientId: String, providerId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "clients", clientId, "installation", "providers", providerId)
    SttpClient.get(path)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def getClientAuthorizationPermissions(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "clients", clientId, "management", "permissions")
    SttpClient.get(path)
  }

  /**
   * Update client Authorization permissions.
   *
   * @param clientId    ID of client (not client-id).
   * @param realm       Name of the Realm.
   * @param permission
   * @return
   */
  def updateClientAuthorizationPermissions(clientId: String, realm: String, permission: ManagementPermission)(implicit authToken: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "clients", clientId, "management", "permissions")
    SttpClient.put(permission, path)
  }

  /**
   * Register a cluster node with the client.
   * Manually register cluster node to this client - usually it’s not needed to call this directly as adapter should handle by sending registration request to Keycloak.
   *
   * @param clientId    ID of client (not client-id).
   * @param realm       Name of the Realm.
   * @param formParams
   * @return
   */
  def registerClusterNode(clientId: String, realm: String, formParams: Map[String, Any])(implicit authToken: String): AsyncApolloResponse[NoContent] = { //TODO Determine formParams type.
    val path = Seq(realm, "clients", clientId, "nodes")
    SttpClient.post(formParams, path)
  }

  /**
   * Unregister a cluster node from the client.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def unregisterClusterNode(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId, "nodes")
    SttpClient.delete(path)
  }

  /**
   * Get application offline session count.
   * Returns a number of offline user sessions associated with this client { "count": number }.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def getOfflineSessionCount(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "clients", clientId, "offline-session-count")
    SttpClient.get(path)
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
  def getOfflineSessions(clientId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None)(implicit authToken: String): AsyncApolloResponse[Seq[UserSession]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(realm, "clients", clientId, "offline-sessions")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Returns optional client scopes.
   * Only name and ids are returned.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def getOptionalClientScopes(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[ClientScope]] = {
    val path = Seq(realm, "clients", clientId, "optional-client-scopes")
    SttpClient.get(path)
  }

  /**
   * ??? //TODO Determine route functionality.
   *
   * @param clientScopeId
   * @param clientId      ID of client (not client-id).
   * @param realm         Name of the Realm.
   * @return
   */
  def updateOptionalClientScope(clientScopeId: String, clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    SttpClient.put(path, Seq.empty[KeyValue])
  }

  /**
   * Deletes an optional client scope.
   *
   * @param clientScopeId
   * @param clientId      ID of client (not client-id).
   * @param realm         Name of the Realm.
   * @return
   */
  def deleteOptionalClientScope(clientScopeId: String, clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "clients", clientId, "optional-client-scopes", clientScopeId)
    SttpClient.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Push the client’s revocation policy to its admin URL.
   * If the client has an admin URL, push revocation policy to it.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def pushRevocationPolicy(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[GlobalRequestResult] = {
    val path = Seq(realm, "clients", clientId, "push-revocation")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Generate a new registration access token for the client.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def generateRegistrationAccessToken(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Client] = {
    val path = Seq(realm, "clients", clientId, "registration-access-token")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Get a user dedicated to the service account.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def getServiceAccountUser(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[User] = {
    val path = Seq(realm, "clients", clientId, "service-account-user")
    SttpClient.get(path)
  }

  /**
   * Get application session count.
   * Returns a number of user sessions associated with this client { "count": number }.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def getSessionCount(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "clients", clientId, "session-count")
    SttpClient.get(path)
  }

  /**
   * Test if registered cluster nodes are available.
   * Tests availability by sending 'ping' request to all cluster nodes.
   *
   * @param clientId ID of client (not client-id).
   * @param realm    Name of the Realm.
   * @return
   */
  def testNodesAvailability(clientId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[GlobalRequestResult] = {
    val path = Seq(realm, "clients", clientId, "test-nodes-available")
    SttpClient.get(path)
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
  def getUserSessions(clientId: String, realm: String, first: Option[Int] = None, max: Option[Int] = None)(implicit authToken: String): AsyncApolloResponse[Seq[UserSession]] = {
    val query = createQuery(
      ("first", first),
      ("max", max)
    )

    val path = Seq(realm, "clients", clientId, "user-sessions")
    SttpClient.get(path, query.to[Seq])
  }
}

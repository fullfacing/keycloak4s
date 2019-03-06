//package com.fullfacing.keycloak4s.services
//
//import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
//import com.fullfacing.apollo.core.protocol.NoContent
//import com.fullfacing.keycloak4s.handles.SttpClient.{UnknownMap, UnknownResponse}
//import com.fullfacing.keycloak4s.handles.KeycloakClient
//import com.fullfacing.keycloak4s.models._
//import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
//
//import scala.collection.immutable.Seq
//
//object Users {
//
//  private val users_path = "users"
//
//  /**
//   * Create a new user Username must be unique
//   *
//   * @param realm
//   * @param user
//   * @return
//   */
//  def createUser(realm: String, user: User)(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = { //TODO return type check
//    val path = Seq(realm, users_path)
//    SttpClient.post[User, UnknownResponse](user, path)
//  }
//
//  /**
//   * Get users Returns a list of users, filtered according to query parameters
//   *
//   * @param realm     realm name (not id!)
//   * @param briefRep
//   * @param email
//   * @param first
//   * @param firstName
//   * @param lastName
//   * @param max       Maximum results size (defaults to 100)
//   * @param search    A String contained in username, first or last name, or email
//   * @param username
//   * @return
//   */
//  def getUsers(realm: String,
//               briefRep: Option[Boolean] = None,
//               email: Option[String] = None,
//               first: Option[Int] = None,
//               firstName: Option[String] = None,
//               lastName: Option[String] = None,
//               max: Option[Int] = None,
//               search: Option[String] = None,
//               username: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[List[User]] = {
//
//    val query = createQuery(
//      ("briefRepresentation", briefRep),
//      ("email", email),
//      ("first", first),
//      ("firstName", firstName),
//      ("lastName", lastName),
//      ("max", max),
//      ("search", search),
//      ("username", username)
//    )
//
//    val path = Seq(realm, users_path)
//    SttpClient.get[List[User]](path, query)
//  }
//
//  /**
//   *
//   * @param realm
//   * @return
//   */
//  def getUsersCount(realm: String)(implicit authToken: String): AsyncApolloResponse[Int] = {
//    val path = Seq(realm, users_path, "count")
//    SttpClient.get(path)
//  }
//
//  /**
//   * Get representation of the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def getUserById(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[User] = {
//    val path = Seq(realm, users_path, userId)
//    SttpClient.get[User](path)
//  }
//
//  /**
//   * Update the user
//   *
//   * @param realm
//   * @param userId
//   * @param updated
//   * @return
//   */
//  def updateUser(realm: String, userId: String, updated: User)(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = {
//    val path = Seq(realm, users_path, userId)
//    SttpClient.put(updated, path)
//  }
//
//  /**
//   * Delete the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def deleteUser(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = {
//    val path = Seq(realm, users_path, userId)
//    SttpClient.delete(path)
//  }
//
//  /**
//   * Get consents granted by the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def getUserConsents(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[List[UnknownMap]] = {
//    val path = Seq(realm, users_path, userId, "consents")
//    SttpClient.get[List[UnknownMap]](path)
//  }
//
//  /**
//   * Revoke consent and offline tokens for particular client from user
//   *
//   * @param realm
//   * @param userId
//   * @param clientId
//   * @return
//   */
//  def revokeClientConsentForUser(realm: String, userId: String, clientId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "consents", clientId)
//    SttpClient.delete(path)
//  }
//
//  /**
//   *
//   * @param realm
//   * @param userId
//   * @param credTypes credentialTypes, required  -- TODO figure out what credential types there are
//   * @return
//   */
//  def disableUserCredentials(realm: String, userId: String, credTypes: List[String])(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "disable-credential-types")
//    SttpClient.put(credTypes, path)
//  }
//
//  /**
//   * Send a update account email to the user.
//   * An email contains a link the user can click to perform a set of required actions.
//   *
//   * The redirectUri and clientId parameters are optional.
//   * If no redirect is given, then there will be no link back to click after actions have completed.
//   * Redirect uri must be a valid uri for the particular clientId.
//   *
//   * @param realm       Realm name (not id!)
//   * @param userId      User id
//   * @param clientId    Client id
//   * @param lifespan    Number of seconds after which the generated token expires
//   * @param redirectUri Redirect uri
//   * @param actions     Required actions the user needs to complete
//   * @return
//   */
//  def executeActionsEmail(realm: String,
//                          userId: String,
//                          clientId: Option[String] = None,
//                          lifespan: Option[Int] = None,
//                          redirectUri: Option[String],
//                          actions: List[String])(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = {
//
//    val query = createQuery(("client_id", clientId), ("lifespan", lifespan), ("redirect_uri", redirectUri))
//
//    val path = Seq(realm, users_path, userId, "execute-actions-email")
//    SttpClient.put(actions, path, query)
//  }
//
//  /**
//   * Get social logins associated with the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def federatedIdentity(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[List[FederatedIdentity]] = {
//    val path = Seq(realm, users_path, userId, "federated-identity")
//    SttpClient.get(path)
//  }
//
//  /**
//   * Add a social login provider to the user
//   *
//   * @param realm
//   * @param userId
//   * @param provider Social login provider id
//   * @param rep
//   * @return
//   */
//  def addUserSocialLoginProvider(realm: String, userId: String, provider: String, rep: FederatedIdentity)(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = {
//    val path = Seq(realm, users_path, userId, "federated-identity", provider)
//    SttpClient.post(rep, path)
//  }
//
//  /**
//   * Remove a social login provider from user
//   *
//   * @param realm
//   * @param userId
//   * @param provider
//   * @return
//   */
//  def removeUserSocialLoginProvider(realm: String, userId: String, provider: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "federated-identity", provider)
//    SttpClient.delete(path)
//  }
//
//  /**
//   *
//   * @param realm
//   * @param userId
//   * @param first
//   * @param max
//   * @param search
//   * @return
//   */
//  def getGroups(realm: String,
//                userId: String,
//                first: Option[Int] = None,
//                max: Option[Int] = None,
//                search: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[List[Group]] = {
//
//    val query = createQuery(("first", first), ("max", max), ("search", search))
//
//    val path = Seq(realm, users_path, userId, "groups")
//    SttpClient.get(path, query)
//  }
//
//  /**
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def groupCount(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[GroupCount] = {
//    val path = Seq(realm, users_path, userId, "groups", "count")
//    SttpClient.get[GroupCount](path)
//  }
//
//  /**
//   *
//   * @param realm
//   * @param userId
//   * @param groupId
//   * @return
//   */
//  def unknown(realm: String, userId: String, groupId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = { //TODO try to figure out what this is
//    val path = Seq(realm, users_path, userId, "groups", groupId)
//    SttpClient.put(path, Seq.empty[KeyValue])
//  }
//
//  def `removeFromGroup???`(realm: String, userId: String, groupId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = { // TODO confirm what this does
//    val path = Seq(realm, users_path, userId, "groups", groupId)
//    SttpClient.delete(path)
//  }
//
//  /**
//   * Impersonate the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def impersonate(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[UnknownMap] = {
//    val path = Seq(realm, users_path, userId, "impersonation")
//    SttpClient.post(path, Seq.empty[KeyValue])
//  }
//
//  /**
//   * Remove all user sessions associated with the user.
//   * Also send notification to all clients that have an admin URL to invalidate the sessions for the particular user.
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def logout(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "logout")
//    SttpClient.post(path, Seq.empty[KeyValue])
//  }
//
//  /** Get offline sessions associated with the user and client
//   *
//   * @param realm
//   * @param userId
//   * @param clientId
//   * @return
//   */
//  def getOfflineSessions(realm: String, userId: String, clientId: String)(implicit authToken: String): AsyncApolloResponse[List[UserSession]] = {
//    val path = Seq(realm, users_path, userId, "offline-sessions", clientId)
//    SttpClient.get(path)
//  }
//
//  /**
//   * Remove TOTP from the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def removeTotp(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "remove-totp")
//    SttpClient.put(path, Seq.empty[KeyValue])
//  }
//
//  /** Set up a new password for the user.
//   *
//   * @param realm
//   * @param userId
//   * @param pass
//   * @return
//   */
//  def resetPassword(realm: String, userId: String, pass: Credential)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
//    val path = Seq(realm, users_path, userId, "reset-password")
//    SttpClient.put(pass, path)
//  }
//
//  /** Send an email-verification email to the user.
//   *  An email contains a link the user can click to verify their email address.
//   *
//   * The redirectUri and clientId parameters are optional. The default for the redirect is the account client.
//   *
//   * @param realm
//   * @param userId
//   * @param clientId
//   * @param redirectUri
//   * @return
//   */
//  def sendVerificationEmail(realm: String,
//                            userId: String,
//                            clientId: Option[String] = None,
//                            redirectUri: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[UnknownResponse] = {
//
//    val query = createQuery(("client_id", clientId), ("redirect_uri",redirectUri))
//
//    val path = Seq(realm, users_path, userId, "send-verify-email")
//    SttpClient.put(path, query)
//  }
//
//  /**
//   * Get sessions associated with the user
//   *
//   * @param realm
//   * @param userId
//   * @return
//   */
//  def getSessions(realm: String, userId: String)(implicit authToken: String): AsyncApolloResponse[List[UserSession]] = {
//    val path = Seq(realm, users_path, userId, "sessions")
//    SttpClient.get(path)
//  }
//}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

class Users[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val users_path = "users"

  /**
   * Create a new user Username must be unique
   *
   * @param realm
   * @param user
   * @return
   */
  def createUser(realm: String, user: User): R[User] = {
    val path = Seq(realm, users_path)
    client.post[User, User](user, path)
  }

  /**
   * Get users Returns a list of users, filtered according to query parameters
   *
   * @param realm     realm name (not id!)
   * @param briefRep
   * @param email
   * @param first
   * @param firstName
   * @param lastName
   * @param max       Maximum results size (defaults to 100)
   * @param search    A String contained in username, first or last name, or email
   * @param username
   * @return
   */
  def getUsers(realm: String,
               briefRep: Option[Boolean] = None,
               email: Option[String] = None,
               first: Option[Int] = None,
               firstName: Option[String] = None,
               lastName: Option[String] = None,
               max: Option[Int] = None,
               search: Option[String] = None,
               username: Option[String] = None): R[List[User]] = {

    val query = createQuery(
      ("briefRepresentation", briefRep),
      ("email", email),
      ("first", first),
      ("firstName", firstName),
      ("lastName", lastName),
      ("max", max),
      ("search", search),
      ("username", username)
    )

    val path = Seq(realm, users_path)
    client.get[List[User]](path, query)
  }

//  /**
//   *
//   * @param realm
//   * @return
//   */
//  def getUsersCount(realm: String): Request[R, S, Int] = { // TODO: Return Type
//    val path = Seq(realm, users_path, "count")
//    client.get[Int](path, Seq.empty[KeyValue])
//  }

  /**
   * Get representation of the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def getUserById(realm: String, userId: String): R[User] = {
    val path = Seq(realm, users_path, userId)
    client.get[User](path, Seq.empty[KeyValue])
  }

  /**
   * Update the user
   *
   * @param realm
   * @param userId
   * @param updated
   * @return
   */
  def updateUser(realm: String, userId: String, updated: User): R[Unit] = {
    val path = Seq(realm, users_path, userId)
    client.putNoContent(updated, path, Seq.empty[KeyValue])
  }

  /**
   * Delete the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def deleteUser(realm: String, userId: String): R[Unit] = {
    val path = Seq(realm, users_path, userId)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Get consents granted by the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def getUserConsents(realm: String, userId: String): R[List[Map[String, String]]] = { // Don't know return type
    val path = Seq(realm, users_path, userId, "consents")
    client.get[List[Map[String, String]]](path, Seq.empty[KeyValue])
  }

  /**
   * Revoke consent and offline tokens for particular client from user
   *
   * @param realm
   * @param userId
   * @param clientId
   * @return
   */
  def revokeClientConsentForUser(realm: String, userId: String, clientId: String): R[Unit] = {
    val path = Seq(realm, users_path, userId, "consents", clientId)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   *
   * @param realm
   * @param userId
   * @param credTypes credentialTypes, required  -- TODO figure out what credential types there are
   * @return
   */
  def disableUserCredentials(realm: String, userId: String, credTypes: List[String]): R[Unit] = {
    val path = Seq(realm, users_path, userId, "disable-credential-types")
    client.putNoContent[List[String]](credTypes, path, Seq.empty[KeyValue])
  }

  /**
   * Send a update account email to the user.
   * An email contains a link the user can click to perform a set of required actions.
   *
   * The redirectUri and clientId parameters are optional.
   * If no redirect is given, then there will be no link back to click after actions have completed.
   * Redirect uri must be a valid uri for the particular clientId.
   *
   * @param realm       Realm name (not id!)
   * @param userId      User id
   * @param clientId    Client id
   * @param lifespan    Number of seconds after which the generated token expires
   * @param redirectUri Redirect uri
   * @param actions     Required actions the user needs to complete
   * @return
   */
  def executeActionsEmail(realm: String,
                          userId: String,
                          clientId: Option[String] = None,
                          lifespan: Option[Int] = None,
                          redirectUri: Option[String],
                          actions: List[String]): R[Unit] = { // Unknown Return Type

    val query = createQuery(("client_id", clientId), ("lifespan", lifespan), ("redirect_uri", redirectUri))

    val path = Seq(realm, users_path, userId, "execute-actions-email")
    client.putNoContent[List[String]](actions, path, query)
  }

  /**
   * Get social logins associated with the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def federatedIdentity(realm: String, userId: String): R[List[FederatedIdentity]] = {
    val path = Seq(realm, users_path, userId, "federated-identity")
    client.get[List[FederatedIdentity]](path, Seq.empty[KeyValue])
  }

  /**
   * Add a social login provider to the user
   *
   * @param realm
   * @param userId
   * @param provider Social login provider id
   * @param rep
   * @return
   */
  def addUserSocialLoginProvider(realm: String, userId: String, provider: String, rep: FederatedIdentity): R[Unit] = { // Unknown Return Type
    val path = Seq(realm, users_path, userId, "federated-identity", provider)
    client.postNoContent(rep, path)
  }

  /**
   * Remove a social login provider from user
   *
   * @param realm
   * @param userId
   * @param provider
   * @return
   */
  def removeUserSocialLoginProvider(realm: String, userId: String, provider: String): R[Unit] = {
    val path = Seq(realm, users_path, userId, "federated-identity", provider)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   *
   * @param realm
   * @param userId
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getGroups(realm: String,
                userId: String,
                first: Option[Int] = None,
                max: Option[Int] = None,
                search: Option[String] = None): R[List[Group]] = {

    val query = createQuery(("first", first), ("max", max), ("search", search))

    val path = Seq(realm, users_path, userId, "groups")
    client.get[List[Group]](path, query)
  }

  /**
   *
   * @param realm
   * @param userId
   * @return
   */
  def groupCount(realm: String, userId: String): R[GroupCount] = {
    val path = Seq(realm, users_path, userId, "groups", "count")
    client.get[GroupCount](path, Seq.empty[KeyValue])
  }

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

  /**
   * Impersonate the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def impersonate(realm: String, userId: String): R[Unit] = { // TODO: Figure out return type
    val path = Seq(realm, users_path, userId, "impersonation")
    client.postNoContent(path)
  }

  /**
   * Remove all user sessions associated with the user.
   * Also send notification to all clients that have an admin URL to invalidate the sessions for the particular user.
   *
   * @param realm
   * @param userId
   * @return
   */
  def logout(realm: String, userId: String): R[Unit] = {
    val path = Seq(realm, users_path, userId, "logout")
    client.postNoContent(path)
  }

  /** Get offline sessions associated with the user and client
   *
   * @param realm
   * @param userId
   * @param clientId
   * @return
   */
  def getOfflineSessions(realm: String, userId: String, clientId: String): R[List[UserSession]] = {
    val path = Seq(realm, users_path, userId, "offline-sessions", clientId)
    client.get[List[UserSession]](path, Seq.empty[KeyValue])
  }

  /**
   * Remove TOTP from the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def removeTotp(realm: String, userId: String): R[Unit] = {
    val path = Seq(realm, users_path, userId, "remove-totp")
    client.put(path, Seq.empty[KeyValue])
  }

  /** Set up a new password for the user.
   *
   * @param realm
   * @param userId
   * @param pass
   * @return
   */
  def resetPassword(realm: String, userId: String, pass: Credential): R[Unit] = {
    val path = Seq(realm, users_path, userId, "reset-password")
    client.putNoContent(pass, path, Seq.empty[KeyValue])
  }

  /** Send an email-verification email to the user.
   *  An email contains a link the user can click to verify their email address.
   *
   * The redirectUri and clientId parameters are optional. The default for the redirect is the account client.
   *
   * @param realm
   * @param userId
   * @param clientId
   * @param redirectUri
   * @return
   */
  def sendVerificationEmail(realm: String,
                            userId: String,
                            clientId: Option[String] = None,
                            redirectUri: Option[String] = None): R[Unit] = { // TODO: Unknown Response

    val query = createQuery(("client_id", clientId), ("redirect_uri",redirectUri))

    val path = Seq(realm, users_path, userId, "send-verify-email")
    client.put(path, query)
  }

  /**
   * Get sessions associated with the user
   *
   * @param realm
   * @param userId
   * @return
   */
  def getSessions(realm: String, userId: String): R[List[UserSession]] = {
    val path = Seq(realm, users_path, userId, "sessions")
    client.get[List[UserSession]](path, Seq.empty[KeyValue])
  }
}
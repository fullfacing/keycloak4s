package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Users[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val users_path = "users"

  /**
   * Create a new user
   * Username must be unique
   *
   * @param user
   * @return
   */
  def createUser(user: User): R[Unit] = {
    val path = Seq(client.realm, users_path)
    client.post(path, user)
  }

  /**
   * Get users Returns a list of users, filtered according to query parameters
   *   realm name (not id!)
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
  def getUsers(briefRep: Option[Boolean] = None,
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

    val path = Seq(client.realm, users_path)
    client.get[List[User]](path, query = query)
  }

  /**
   *
   * @return
   */
  def getUsersCount(): R[Int] = {
    val path = Seq(client.realm, users_path, "count")
    client.get[Int](path)
  }

  /**
   * Get representation of the user
   *
   * @param userId
   * @return
   */
  def getUserById(userId: String): R[User] = {
    val path = Seq(client.realm, users_path, userId)
    client.get[User](path)
  }

  /**
   * Update the user
   *
   * @param userId
   * @param updated
   * @return
   */
  def updateUser(userId: String, updated: User): R[Unit] = {
    val path = Seq(client.realm, users_path, userId)
    client.put(path, updated)
  }

  /**
   * Delete the user
   *
   * @param userId
   * @return
   */
  def deleteUser(userId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId)
    client.delete(path)
  }

  /**
   * Get consents granted by the user
   *
   * @param userId
   * @return
   */
  def getUserConsents(userId: String): R[List[UserConsent]] = {
    val path = Seq(client.realm, users_path, userId, "consents")
    client.get[List[UserConsent]](path)
  }

  /**
   * Revoke consent and offline tokens for particular client from user
   *
   * @param userId
   * @param clientId
   * @return
   */
  def revokeClientConsentForUser(userId: String, clientId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "consents", clientId)
    client.delete(path)
  }

  /**
   *
   * @param userId
   * @param credentialTypes See User model field 'disableableCredentialTypes'.
   * @return
   */
  def disableUserCredentials(userId: String, credentialTypes: List[String]): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "disable-credential-types")
    client.put[List[String], Unit](path, credentialTypes)
  }

  /**
   * Send a update account email to the user.
   * An email contains a link the user can click to perform a set of required actions.
   *
   * The redirectUri and clientId parameters are optional.
   * If no redirect is given, then there will be no link back to click after actions have completed.
   * Redirect uri must be a valid uri for the particular clientId.
   *   Realm name (not id!)
   * @param userId      User id
   * @param clientId    Client id
   * @param lifespan    Number of seconds after which the generated token expires
   * @param redirectUri Redirect uri
   * @param actions     Required actions the user needs to complete
   * @return
   */
  def executeActionsEmail(userId: String,
                          clientId: Option[String] = None,
                          lifespan: Option[Int] = None,
                          redirectUri: Option[String],
                          actions: List[String]): R[Unit] = {

    val query = createQuery(("client_id", clientId), ("lifespan", lifespan), ("redirect_uri", redirectUri))

    val path = Seq(client.realm, users_path, userId, "execute-actions-email")
    client.put[List[String], Unit](path, actions, query)
  }

  /**
   * Get social logins associated with the user
   *
   * @param userId
   * @return
   */
  def federatedIdentity(userId: String): R[List[FederatedIdentity]] = {
    val path = Seq(client.realm, users_path, userId, "federated-identity")
    client.get[List[FederatedIdentity]](path)
  }

  /**
   * Add a social login provider to the user
   *
   * @param userId
   * @param provider Social login provider id
   * @param rep
   * @return
   */
  def addUserSocialLoginProvider(userId: String, provider: String, rep: FederatedIdentity): R[Unit] = { // Unknown Return Type
    val path = Seq(client.realm, users_path, userId, "federated-identity", provider)
    client.post[FederatedIdentity, Unit](path, rep)
  }

  /**
   * Remove a social login provider from user
   *
   * @param userId
   * @param provider
   * @return
   */
  def removeUserSocialLoginProvider(userId: String, provider: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "federated-identity", provider)
    client.delete(path)
  }

  /**
   *
   * @param userId
   * @param first
   * @param max
   * @param search
   * @return
   */
  def getGroups(userId: String,
                first: Option[Int] = None,
                max: Option[Int] = None,
                search: Option[String] = None): R[List[Group]] = {

    val query = createQuery(("first", first), ("max", max), ("search", search))

    val path = Seq(client.realm, users_path, userId, "groups")
    client.get[List[Group]](path, query = query)
  }

  /**
   *
   * @param userId
   * @return
   */
  def groupCount(userId: String): R[Count] = {
    val path = Seq(client.realm, users_path, userId, "groups", "count")
    client.get[Count](path)
  }

  /**
   * Add user to specified group
   *   Name of the realm
   * @param userId    Id of user to add to the group
   * @param groupId   Id of the group the user is to be added to
   * @return
   */
  def joinGroup(userId: String, groupId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "groups", groupId)
    client.put(path)
  }


  /**
   * removeMembership
   *   Name of the realm.
   * @param userId    Id of user to remove from the group.
   * @param groupId   Id of the group from which the user is to be removed.
   * @return
   */
  def removeFromGroup(userId: String, groupId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "groups", groupId)
    client.delete(path)
  }

  /**
   * Impersonate the user
   *
   * @param userId
   * @return
   */
  def impersonate(userId: String): R[ImpersonationResponse] = {
    val path = Seq(client.realm, users_path, userId, "impersonation")
    client.post[Unit, ImpersonationResponse](path)
  }

  /**
   * Remove all user sessions associated with the user.
   * Also send notification to all clients that have an admin URL to invalidate the sessions for the particular user.
   *
   * @param userId
   * @return
   */
  def logout(userId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "logout")
    client.post(path)
  }

  /** Get offline sessions associated with the user and client
   *
   * @param userId
   * @param clientId
   * @return
   */
  def getOfflineSessions(userId: String, clientId: String): R[List[UserSession]] = {
    val path = Seq(client.realm, users_path, userId, "offline-sessions", clientId)
    client.get[List[UserSession]](path)
  }

  /**
   * Remove TOTP from the user
   *
   * @param userId
   * @return
   */
  def removeTotp(userId: String): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "remove-totp")
    client.put(path)
  }

  /** Set up a new password for the user.
   *
   * @param userId
   * @param pass
   * @return
   */
  def resetPassword(userId: String, pass: Credential): R[Unit] = {
    val path = Seq(client.realm, users_path, userId, "reset-password")
    client.put(path, pass)
  }

  /** Send an email-verification email to the user.
   *  An email contains a link the user can click to verify their email address.
   *
   * The redirectUri and clientId parameters are optional. The default for the redirect is the account client.
   *
   * @param userId
   * @param clientId
   * @param redirectUri
   * @return
   */
  def sendVerificationEmail(userId: String,
                            clientId: Option[String] = None,
                            redirectUri: Option[String] = None): R[Unit] = {

    val query = createQuery(("client_id", clientId), ("redirect_uri",redirectUri))

    val path = Seq(client.realm, users_path, userId, "send-verify-email")
    client.put(path, query = query)
  }

  /**
   * Get sessions associated with the user
   *
   * @param userId
   * @return
   */
  def getSessions(userId: String): R[List[UserSession]] = {
    val path = Seq(client.realm, users_path, userId, "sessions")
    client.get[List[UserSession]](path)
  }
}
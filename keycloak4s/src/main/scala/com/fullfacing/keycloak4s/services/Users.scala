package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Users[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) {

  private val users_path = "users"

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  def create(user: User.Create): R[Either[KeycloakError, User]] = {
    val path = Seq(realm, users_path)
    client.post[User.Create, User](path, user)
  }

  def fetch(briefRep: Option[Boolean] = None, username: Option[String] = None, email: Option[String] = None, first: Option[Int] = None,
            firstName: Option[String] = None, lastName: Option[String] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, List[User]]] = {

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
    client.get[List[User]](path, query = query)
  }

  def fetchById(userId: UUID): R[Either[KeycloakError, User]] = {
    val path = Seq(realm, users_path, userId.toString)
    client.get[User](path)
  }

  def update(userId: UUID, user: User): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString)
    client.put(path, user)
  }

  def delete(userId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId)
    client.delete(path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Counts ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  def count(): R[Either[KeycloakError, Int]] = {
    val path = Seq(realm, users_path, "count")
    client.get[Int](path)
  }

  def countGroups(userId: UUID): R[Either[KeycloakError, Count]] = {
    val path = Seq(realm, users_path, userId.toString, "groups", "count")
    client.get[Count](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Consent ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  def fetchUserConsent(userId: UUID): R[Either[KeycloakError, List[UserConsent]]] = {
    val path = Seq(realm, users_path, userId.toString, "consents")
    client.get[List[UserConsent]](path)
  }
  
  def revokeClientConsentForUser(userId: UUID, clientId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "consents", clientId)
    client.delete(path)
  }

  // -------------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Federated Identity ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------------------- //
  def createFederatedIdentity(userId: UUID, provider: String, rep: FederatedIdentity): R[Either[KeycloakError, Unit]] = { // Unknown Return Type
    val path = Seq(realm, users_path, userId.toString, "federated-identity", provider)
    client.post[FederatedIdentity, Unit](path, rep)
  }
  
  def fetchFederatedIdentities(userId: UUID): R[Either[KeycloakError, List[FederatedIdentity]]] = {
    val path = Seq(realm, users_path, userId.toString, "federated-identity")
    client.get[List[FederatedIdentity]](path)
  }

  def removeFederatedIdentityProvider(userId: UUID, provider: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "federated-identity", provider)
    client.delete(path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Emails ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //

  /** Send an email-verification email to the user. An email contains a link the user can click to verify their email address.
    *
    * The redirectUri and clientId parameters are optional. The default for the redirect is the account client.
    */
  def sendVerificationEmail(userId: UUID, clientId: Option[String] = None, redirectUri: Option[String] = None): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("client_id", clientId), ("redirect_uri",redirectUri))
    val path = Seq(realm, users_path, userId.toString, "send-verify-email")
    client.put(path, query = query)
  }

  /**
    * Send a update account email to the user, the email contains a link that the user can click on to perform a set of
    * required actions.
    *
    * The redirectUri and clientId parameters are optional. If no redirect is given, then there will be no link back to
    * click after actions have completed. The Redirect URI must be a valid URI for the particular clientId.
    */
  def sendActionsEmail(userId: UUID, clientId: Option[String] = None, lifespan: Option[Int] = None,
                       redirectUri: Option[String], actions: List[String]): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("client_id", clientId), ("lifespan", lifespan), ("redirect_uri", redirectUri))
    val path = Seq(realm, users_path, userId.toString, "execute-actions-email")
    client.put[List[String], Unit](path, actions, query)
  }



  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Groups ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  def fetchGroups(userId: UUID, first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, List[Group]]] = {
    val query = createQuery(("first", first), ("max", max), ("search", search))
    val path = Seq(realm, users_path, userId.toString, "groups")
    client.get[List[Group]](path, query = query)
  }
  
  def addToGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "groups", groupId.toString)
    client.put(path)
  }
  
  def removeFromGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "groups", groupId.toString)
    client.delete(path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Roles ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  def fetchRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings", "realm")
    client.get[List[Role]](path)
  }

  def fetchUserRoleMappings(userId: UUID): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings")
    client.get[Mappings](path)
  }

  def addRealmRoles(userId: UUID, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings", "realm")
    client.post[List[Role], Unit](path, roles)
  }

  def removeRealmRoles(userId: UUID, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings", "realm")
    client.delete[List[Role], Unit](path, roles)
  }

  def fetchAvailableRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  def fetchEffectiveRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId.toString, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  // ---------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Sessions ------------------------------------------------ //
  // ---------------------------------------------------------------------------------------------------------- //
  
  def fetchSessions(userId: UUID): R[Either[KeycloakError, List[UserSession]]] = {
    val path = Seq(realm, users_path, userId.toString, "sessions")
    client.get[List[UserSession]](path)
  }
  
  def fetchOfflineSessions(userId: UUID, clientId: String): R[Either[KeycloakError, List[UserSession]]] = {
    val path = Seq(realm, users_path, userId.toString, "offline-sessions", clientId)
    client.get[List[UserSession]](path)
  }
  
  def removeTotp(realm: String, userId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId, "remove-totp")
    client.put(path)
  }
  
  def resetPassword(userId: UUID, credential: Credential): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "reset-password")
    client.put(path, credential)
  }
  
  def disableUserCredentials(userId: UUID, credentialTypes: List[String]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "disable-credential-types")
    client.put[List[String], Unit](path, credentialTypes)
  }
  
  def impersonate(userId: UUID): R[Either[KeycloakError, ImpersonationResponse]] = {
    val path = Seq(realm, users_path, userId.toString, "impersonation")
    client.post[Unit, ImpersonationResponse](path)
  }
  
  def logout(userId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, users_path, userId.toString, "logout")
    client.post(path)
  }
}
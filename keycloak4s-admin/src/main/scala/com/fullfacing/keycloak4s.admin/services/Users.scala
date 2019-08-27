package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}

import scala.collection.immutable.Seq

class Users[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  /** Create a new user in the realm. */
  def create(user: User.Create): R[Either[KeycloakError, UUID]] = {
    val path = Seq(client.realm, "users")
    Concurrent[R].map(client.post[Headers](path, user))(extractUuid)
  }

  /** Compound function that creates and then retrieves a new user. */
  def createAndRetrieve(user: User.Create): R[Either[KeycloakError, User]] =
    Concurrent[R].flatMap(create(user)) { _ =>
      Concurrent[R].map(fetch(username = Some(user.username))) { response =>
        response.flatMap(_.headOption.toRight(Exceptions.RESOURCE_NOT_FOUND("User")))
      }
    }

  /** Fetches all users in the realm filtered according to the given parameters. */
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

    val path = Seq(client.realm, "users")
    client.get[List[User]](path, query = query)
  }

  /** Fetches the user with the given user ID. */
  def fetchById(userId: UUID): R[Either[KeycloakError, User]] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.get[User](path)
  }

  /** Update the details of a user. */
  def update(userId: UUID, user: User.Update): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.put[Unit](path, user)
  }

  /** Delete the specified user. */
  def delete(userId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Counts ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  /** Retrieve the number of users in the realm. */
  def count(): R[Either[KeycloakError, Int]] = {
    val path = Seq(client.realm, "users", "count")
    client.get[Int](path)
  }

  /** Retrieve the number of groups to which the user belongs. */
  def countGroups(userId: UUID): R[Either[KeycloakError, Count]] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", "count")
    client.get[Count](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Consent ----------------------------------------------- //
  // -------------------------------------------------------------------------------------------------------- //
  /** Get consents granted by the user. */
  def fetchUserConsent(userId: UUID): R[Either[KeycloakError, List[UserConsent]]] = {
    val path = Seq(client.realm, "users", userId.toString, "consents")
    client.get[List[UserConsent]](path)
  }

  /** Revoke consent and offline tokens for the given client from the user. */
  def revokeClientConsentForUser(userId: UUID, clientId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "consents", clientId)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Federated Identity ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------------------- //
  /** Add a social login provider to the user */
  def createFederatedIdentity(userId: UUID, provider: String, rep: FederatedIdentity): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "federated-identity", provider)
    client.post[Unit](path, rep)
  }

  /** Fetch social logins associated with the user. */
  def fetchFederatedIdentities(userId: UUID): R[Either[KeycloakError, List[FederatedIdentity]]] = {
    val path = Seq(client.realm, "users", userId.toString, "federated-identity")
    client.get[List[FederatedIdentity]](path)
  }

  /** Remove a social login provider from user. */
  def removeFederatedIdentityProvider(userId: UUID, provider: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "federated-identity", provider)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Emails ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //

  /**
   * Send an email-verification email to the user. An email contains a link the user can click to verify their email address.
   *
   * The redirectUri and clientId parameters are optional. The default for the redirect is the account client.
   */
  def sendVerificationEmail(userId: UUID, clientId: Option[String] = None, redirectUri: Option[String] = None): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("client_id", clientId), ("redirect_uri",redirectUri))
    val path = Seq(client.realm, "users", userId.toString, "send-verify-email")
    client.put[Unit](path, query = query)
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
    val path = Seq(client.realm, "users", userId.toString, "execute-actions-email")
    client.put[Unit](path, actions, query)
  }



  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Groups ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  /** Fetch all groups to which the user belongs. */
  def fetchGroups(userId: UUID, first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): R[Either[KeycloakError, List[Group]]] = {
    val query = createQuery(("first", first), ("max", max), ("search", search))
    val path = Seq(client.realm, "users", userId.toString, "groups")
    client.get[List[Group]](path, query = query)
  }

  /** Add the user to the specified group. */
  def addToGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", groupId.toString)
    client.put[Unit](path)
  }

  /** Remove the user from the specified group. */
  def removeFromGroup(userId: UUID, groupId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", groupId.toString)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Roles ------------------------------------------------- //
  // -------------------------------------------------------------------------------------------------------- //
  /** Retrieve all roles assigned to the user. */
  def fetchRoles(userId: UUID): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings")
    client.get[Mappings](path)
  }

  // --- Realm Level Roles --- //
  /** Retrieve all realm level roles assigned to the user. */
  def fetchRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.get[List[Role]](path)
  }

  /** Assign realm roles to the user. */
  def addRealmRoles(userId: UUID, roles: List[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.post[Unit](path, roles)
  }

  /** Un-assign realm roles from the user. */
  def removeRealmRoles(userId: UUID, roles: List[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.delete[Unit](path, roles)
  }

  /** Retrieve all available realm roles that can be assigned to the user. */
  def fetchAvailableRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /** Retrieve all realm roles assigned to the user and all their sub roles. */
  def fetchEffectiveRealmRoles(userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }

  // --- Client Level Roles --- //
  /** Retrieve all client level roles (from the specified client) assigned to the user */
  def fetchClientRoles(clientId: UUID, userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.get[List[Role]](path)
  }

  /** Assign client roles to the user. */
  def addClientRoles(clientId: UUID, userId: UUID, roles: Seq[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.post[Unit](path, roles)
  }

  /** Un-assign client roles from the user. */
  def removeClientRoles(clientId: UUID, userId: UUID, roles: Seq[Role.Mapping]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.delete[Unit](path, roles)
  }

  /** Retrieve all available client level roles that can be assigned to the user. */
  def fetchAvailableClientRoles(clientId: UUID, userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString, "available")
    client.get[List[Role]](path)
  }

  /** Retrieve all client level roles assigned to the user and all their sub roles. */
  def fetchEffectiveClientRoles(clientId: UUID, userId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString, "composite")
    client.get[List[Role]](path)
  }

  // ---------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Sessions ------------------------------------------------ //
  // ---------------------------------------------------------------------------------------------------------- //

  /** Fetch sessions associated with the user. */
  def fetchSessions(userId: UUID): R[Either[KeycloakError, List[UserSession]]] = {
    val path = Seq(client.realm, "users", userId.toString, "sessions")
    client.get[List[UserSession]](path)
  }

  /** Get offline sessions associated with the user and client. */
  def fetchOfflineSessions(userId: UUID, clientId: UUID): R[Either[KeycloakError, List[UserSession]]] = {
    val path = Seq(client.realm, "users", userId.toString, "offline-sessions", clientId.toString)
    client.get[List[UserSession]](path)
  }

  /** Remove temporary one time pin from the user */
  def removeTotp(userId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "remove-totp")
    client.put[Unit](path)
  }

  /** Set up a new password for the user. */
  def resetPassword(userId: UUID, credential: Credential): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "reset-password")
    client.put[Unit](path, credential)
  }

  /** Disable a user's credentials of the specified types. */
  def disableUserCredentials(userId: UUID, credentialTypes: List[String]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "disable-credential-types")
    client.put[Unit](path, credentialTypes)
  }

  /** Impersonate the user */
  def impersonate(userId: UUID): R[Either[KeycloakError, ImpersonationResponse]] = {
    val path = Seq(client.realm, "users", userId.toString, "impersonation")
    client.post[ImpersonationResponse](path)
  }

  /**
   * Remove all user sessions associated with the user.
   * Also send notification to all clients that have an admin URL to invalidate the sessions for the particular user.
   */
  def logout(userId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "users", userId.toString, "logout")
    client.post[Unit](path)
  }
}
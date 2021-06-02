package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.enums.RequiredAction
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class Users(implicit client: KeycloakClient) {

  // ------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------ //
  /** Create a new user in the realm. */
  def create(user: User.Create): IO[KeycloakError, UUID] = {
    val path = Seq(client.realm, "users")
    client.post[Headers](path, user).map(extractUuid).flatMap(IO.fromEither)
  }

  /** Compound function that creates and then retrieves a new user. */
  def createAndRetrieve(user: User.Create): IO[KeycloakError, User] = {
    create(user).flatMap(id => fetchById(id))
  }

  /** Fetches all users in the realm filtered according to the given parameters. */
  def fetch(briefRep: Option[Boolean] = None, username: Option[String] = None, email: Option[String] = None, first: Option[Int] = None,
            firstName: Option[String] = None, lastName: Option[String] = None, max: Option[Int] = None, search: Option[String] = None): IO[KeycloakError, List[User]] = {

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
  def fetchById(userId: UUID): IO[KeycloakError, User] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.get[User](path)
  }

  /** Update the details of a user. */
  def update(userId: UUID, user: User.Update): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.put[Unit](path, user)
  }

  /** Delete the specified user. */
  def delete(userId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Counts ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  /** Retrieve the number of users in the realm. */
  def count(): IO[KeycloakError, Int] = {
    val path = Seq(client.realm, "users", "count")
    client.get[Int](path)
  }

  /** Retrieve the number of groups to which the user belongs. */
  def countGroups(userId: UUID): IO[KeycloakError, Count] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", "count")
    client.get[Count](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Consent ----------------------------------------------- //
  // -------------------------------------------------------------------------------------------------------- //
  /** Get consents granted by the user. */
  def fetchUserConsent(userId: UUID): IO[KeycloakError, List[UserConsent]] = {
    val path = Seq(client.realm, "users", userId.toString, "consents")
    client.get[List[UserConsent]](path)
  }

  /** Revoke consent and offline tokens for the given client from the user. */
  def revokeClientConsentForUser(userId: UUID, clientId: String): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "consents", clientId)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ---------------------------------------------- Credentials --------------------------------------------- //
  // -------------------------------------------------------------------------------------------------------- //

  /** Fetch a user's credentials. */
  def fetchCredentials(userId: UUID): IO[KeycloakError, List[Credential]] = {
    val path = Seq(client.realm, "users", userId.toString, "credentials")
    client.get[List[Credential]](path)
  }

  /** Remove the given credential from a user. */
  def revokeCredential(userId: UUID, credentialId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "credentials", credentialId.toString)
    client.delete[Unit](path)
  }

  /**
   * Move a credential to a position after another credential.
   *
   * @param bCredentialId The credential that will be the previous element in the list.
   *                      If set to null, the moved credential will be the first element in the list.
   */
  def moveCredential(userId: UUID, credentialId: UUID, bCredentialId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "credentials", credentialId.toString, "moveAfter", bCredentialId.toString)
    client.post[Unit](path)
  }

  /** Move a credential to the first position of the user's credential list. */
  def moveCredentialToFirst(userId: UUID, credentialId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "credentials", credentialId.toString, "moveToFirst")
    client.post[Unit](path)
  }

  /** Update the label of a user's credential. */
  def updateCredentialLabel(userId: UUID, credentialId: UUID, label: String): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "credentials", credentialId.toString, "userLabel")
    client.put[Unit](path, label)
  }

  // -------------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Federated Identity ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------------------- //
  /** Add a social login provider to the user */
  def createFederatedIdentity(userId: UUID, provider: String, rep: FederatedIdentity): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "federated-identity", provider)
    client.post[Unit](path, rep)
  }

  /** Fetch social logins associated with the user. */
  def fetchFederatedIdentities(userId: UUID): IO[KeycloakError, List[FederatedIdentity]] = {
    val path = Seq(client.realm, "users", userId.toString, "federated-identity")
    client.get[List[FederatedIdentity]](path)
  }

  /** Remove a social login provider from user. */
  def removeFederatedIdentityProvider(userId: UUID, provider: String): IO[KeycloakError, Unit] = {
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
  def sendVerificationEmail(userId: UUID, clientId: Option[String] = None, redirectUri: Option[String] = None): IO[KeycloakError, Unit] = {
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
  def sendActionsEmail(userId: UUID,
                       actions: List[RequiredAction],
                       clientId: Option[String] = None,
                       lifespan: Option[Int] = None,
                       redirectUri: Option[String] = None): IO[KeycloakError, Unit] = {
    val query = createQuery(("client_id", clientId), ("lifespan", lifespan), ("redirect_uri", redirectUri))
    val path = Seq(client.realm, "users", userId.toString, "execute-actions-email")
    client.put[Unit](path, actions, query)
  }



  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Groups ------------------------------------------------ //
  // -------------------------------------------------------------------------------------------------------- //
  /** Fetch all groups to which the user belongs. */
  def fetchGroups(userId: UUID, first: Option[Int] = None, max: Option[Int] = None, search: Option[String] = None): IO[KeycloakError, List[Group]] = {
    val query = createQuery(("first", first), ("max", max), ("search", search))
    val path = Seq(client.realm, "users", userId.toString, "groups")
    client.get[List[Group]](path, query = query)
  }

  /** Add the user to the specified group. */
  def addToGroup(userId: UUID, groupId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", groupId.toString)
    client.put[Unit](path)
  }

  /** Remove the user from the specified group. */
  def removeFromGroup(userId: UUID, groupId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "groups", groupId.toString)
    client.delete[Unit](path)
  }

  // -------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Roles ------------------------------------------------- //
  // -------------------------------------------------------------------------------------------------------- //
  /** Retrieve all roles assigned to the user. */
  def fetchRoles(userId: UUID): IO[KeycloakError, Mappings] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings")
    client.get[Mappings](path)
  }

  // --- Realm Level Roles --- //
  /** Retrieve all realm level roles assigned to the user. */
  def fetchRealmRoles(userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.get[List[Role]](path)
  }

  /** Assign realm roles to the user. */
  def addRealmRoles(userId: UUID, roles: List[Role.Mapping]): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.post[Unit](path, roles)
  }

  /** Un-assign realm roles from the user. */
  def removeRealmRoles(userId: UUID, roles: List[Role.Mapping]): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm")
    client.delete[Unit](path, roles)
  }

  /** Retrieve all available realm roles that can be assigned to the user. */
  def fetchAvailableRealmRoles(userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm", "available")
    client.get[List[Role]](path)
  }

  /** Retrieve all realm roles assigned to the user and all their sub roles. */
  def fetchEffectiveRealmRoles(userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "realm", "composite")
    client.get[List[Role]](path)
  }

  // --- Client Level Roles --- //
  /** Retrieve all client level roles (from the specified client) assigned to the user */
  def fetchClientRoles(clientId: UUID, userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.get[List[Role]](path)
  }

  /** Assign client roles to the user. */
  def addClientRoles(clientId: UUID, userId: UUID, roles: Seq[Role.Mapping]): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.post[Unit](path, roles)
  }

  /** Un-assign client roles from the user. */
  def removeClientRoles(clientId: UUID, userId: UUID, roles: Seq[Role.Mapping]): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString)
    client.delete[Unit](path, roles)
  }

  /** Retrieve all available client level roles that can be assigned to the user. */
  def fetchAvailableClientRoles(clientId: UUID, userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString, "available")
    client.get[List[Role]](path)
  }

  /** Retrieve all client level roles assigned to the user and all their sub roles. */
  def fetchEffectiveClientRoles(clientId: UUID, userId: UUID): IO[KeycloakError, List[Role]] = {
    val path = Seq(client.realm, "users", userId.toString, "role-mappings", "clients", clientId.toString, "composite")
    client.get[List[Role]](path)
  }

  // ---------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Sessions ------------------------------------------------ //
  // ---------------------------------------------------------------------------------------------------------- //

  /** Fetch sessions associated with the user. */
  def fetchSessions(userId: UUID): IO[KeycloakError, List[UserSession]] = {
    val path = Seq(client.realm, "users", userId.toString, "sessions")
    client.get[List[UserSession]](path)
  }

  /** Get offline sessions associated with the user and client. */
  def fetchOfflineSessions(userId: UUID, clientId: UUID): IO[KeycloakError, List[UserSession]] = {
    val path = Seq(client.realm, "users", userId.toString, "offline-sessions", clientId.toString)
    client.get[List[UserSession]](path)
  }

  /** Set up a new password for the user. */
  def resetPassword(userId: UUID, credential: Credential): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "reset-password")
    client.put[Unit](path, credential)
  }

  /** Impersonate the user */
  def impersonate(userId: UUID): IO[KeycloakError, ImpersonationResponse] = {
    val path = Seq(client.realm, "users", userId.toString, "impersonation")
    client.post[ImpersonationResponse](path)
  }

  /**
   * Remove all user sessions associated with the user.
   * Also send notification to all clients that have an admin URL to invalidate the sessions for the particular user.
   */
  def logout(userId: UUID): IO[KeycloakError, Unit] = {
    val path = Seq(client.realm, "users", userId.toString, "logout")
    client.post[Unit](path)
  }
}
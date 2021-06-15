package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class Roles(implicit client: KeycloakClient) {

  // ------------------------------------------------------------------------------------------------------------------- //
  // -------------------------------------------------- Client Roles --------------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------------------- //
  object ClientLevel {

    // --- CRUD --- //
    /** Create a client level role for the given client. */
    def create(clientId: UUID, role: Role.Create): IO[KeycloakError, String] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles")
      client.post[Headers](path, role.copy(clientRole = true)).map(extractString).flatMap(IO.fromEither)
    }

    /** Creates and then fetches a new client level role for the given client. */
    def createAndRetrieve(clientId: UUID, role: Role.Create): IO[KeycloakError, Role] = {
      create(clientId, role).flatMap(_ => fetchByName(clientId, role.name))
    }

    /** Retrieve all roles from the given client. */
    def fetch(clientId: UUID): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles")
      client.get[List[Role]](path)
    }

    /** Retrieve the role with the specified name from the given client. */
    def fetchByName(clientId: UUID, name: String): IO[KeycloakError, Role] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name)
      client.get[Role](path)
    }

    /** Update the details of the role with the specified name from the given client. */
    def update(clientId: UUID, name: String, role: Role.Update): IO[KeycloakError, Unit] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name)
      client.put[Unit](path, role)
    }

    /** Delete the role with the specified name from the given client. */
    def delete(clientId: UUID, name: String): IO[KeycloakError, Unit] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name)
      client.delete[Unit](path)
    }

    /** Retrieve all users that have been assigned the given role. */
    def fetchUsers(clientId: UUID, name: String, first: Option[Int], max: Option[Int]): IO[KeycloakError, List[User]] = {
      val path: Path  = Seq(client.realm, "clients", clientId, "roles", name, "users")
      val query = createQuery(("first", first), ("max", max))
      client.get[List[User]](path, query = query)
    }

    /** Retrieve all groups that have been assigned the given role. */
    def fetchGroups(clientId: UUID, name: String, first: Option[Int], max: Option[Int], full: Option[Boolean]): IO[KeycloakError, List[Group]] = {
      val path: Path  = Seq(client.realm, "clients", clientId, "roles", name, "groups")
      val query = createQuery(("first", first), ("max", max), ("full", full))
      client.get[List[Group]](path, query)
    }

    // --- Composites --- //
    /** Map the provided sub roles to this role. */
    def addCompositeRoles(clientId: UUID, name: String, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
      val body = roleIds.map(Role.Id)
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "composites")
      client.post[Unit](path, body)
    }

    /** Retrieve all the sub roles mapped to the given client role. */
    def fetchCompositeRoles(clientId: UUID, name: String): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "composites")
      client.get[List[Role]](path)
    }

    /** Remove the provided sub roles from the given client level role. */
    def removeCompositeRoles(clientId: UUID, name: String, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
      val body = roleIds.map(Role.Id)
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "composites")
      client.delete[Unit](path, body)
    }

    /** Retrieve all client level sub roles mapped to the given client role. */
    def fetchClientCompositeRoles(clientId: UUID, name: String, compositeClientId: UUID): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "composites", "clients", compositeClientId)
      client.get[List[Role]](path)
    }

    /** Retrieve all realm level sub roles mapped to the given client role. */
    def fetchRealmCompositeRoles(clientId: UUID, name: String): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "composites", "realm")
      client.get[List[Role]](path)
    }

    // --- Permissions --- //
    /** Retrieve the management permission details of the role. */
    def fetchManagementPermissions(clientId: UUID, name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "management", "permissions")
      client.get[ManagementPermission](path)
    }

    /** Enable management permissions for the role. */
    def enableManagementPermissions(clientId: UUID, name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "management", "permissions")
      client.put[ManagementPermission](path, ManagementPermission.Enable(true))
    }

    /** Disable management permissions for the role. */
    def disableManagementPermissions(clientId: UUID, name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "clients", clientId, "roles", name, "management", "permissions")
      client.put[ManagementPermission](path, ManagementPermission.Enable(false))
    }
  }

  // ------------------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------------- Realm Roles --------------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------------------ //
  object RealmLevel {

    // --- CRUD --- //
    /** Create a new realm level role. */
    def create(role: Role.Create): IO[KeycloakError, Unit] = {
      val path: Path = Seq(client.realm, "roles")
      client.post[Unit](path, role.copy(clientRole = false))
    }

    /** Create and fetch a new realm level role. */
    def createAndRetrieve(role: Role.Create): IO[KeycloakError, Role] = {
      create(role).flatMap(_ => fetchByName(role.name))
    }

    /** Retrieve all realm level roles in the realm */
    def fetch(): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "roles")
      client.get[List[Role]](path)
    }

    /** Retrieve the role with the given role name. */
    def fetchByName(name: String): IO[KeycloakError, Role] = {
      val path: Path = Seq(client.realm, "roles", name)
      client.get[Role](path)
    }

    /** Update the details of the given role. */
    def update(name: String, role: Role.Update): IO[KeycloakError, Unit] = {
      val path: Path = Seq(client.realm, "roles", name)
      client.put[Unit](path, role)
    }

    /** Delete the given role from the realm. */
    def delete(name: String): IO[KeycloakError, Unit] = {
      val path: Path = Seq(client.realm, "roles", name)
      client.delete[Unit](path)
    }

    /** Retrieve all users that have been assigned the given role. */
    def fetchUsers(name: String, first: Option[Int], max: Option[Int]): IO[KeycloakError, List[User]] = {
      val path: Path = Seq(client.realm, "roles", name, "users")
      val query = createQuery(("first", first), ("max", max))
      client.get[List[User]](path, query = query)
    }

    /** Retrieve all groups that have been assigned the given role. */
    def fetchGroups(name: String, first: Option[Int], max: Option[Int], full: Option[Boolean]): IO[KeycloakError, List[Group]] = {
      val path: Path = Seq(client.realm, "roles", name, "groups")
      val query = createQuery(("first", first), ("max", max), ("full", full))
      client.get[List[Group]](path, query)
    }

    // --- Composites --- //
    /** Map the provided sub roles to the given realm role. */
    def addCompositeRoles(name: String, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
      val body = roleIds.map(Role.Id)
      val path: Path = Seq(client.realm, "roles", name, "composites")
      client.post[Unit](path, body)
    }

    /** Remove the provided sub roles from the given realm role. */
    def removeCompositeRoles(name: String, roleIds: List[UUID]): IO[KeycloakError, Unit] = {
      val body = roleIds.map(Role.Id)
      val path: Path = Seq(client.realm, "roles", name, "composites")
      client.delete[Unit](path, body)
    }

    /** Retrieve all sub roles mapped to the given role. */
    def fetchCompositeRoles(name: String): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "roles", name, "composites")
      client.get[List[Role]](path)
    }

    /** Retrieve all client level sub roles mapped to the given role. */
    def fetchClientCompositeRoles(name: String, clientId: UUID): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "roles", name, "composites", "clients", clientId)
      client.get[List[Role]](path)
    }

    /** Retrieve all realm level sub roles mapped to the given role. */
    def fetchRealmCompositeRoles(name: String): IO[KeycloakError, List[Role]] = {
      val path: Path = Seq(client.realm, "roles", name, "composites", "realm")
      client.get[List[Role]](path)
    }

    // --- Permissions --- //
    /** Retrieve the management permission details of the role. */
    def fetchManagementPermissions(name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "roles", name, "management", "permissions")
      client.get[ManagementPermission](path)
    }

    /** Enable management permissions for the role. */
    def enableManagementPermissions(name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "roles", name, "management", "permissions")
      client.put[ManagementPermission](path, ManagementPermission.Enable(true))
    }

    /** Disable management permissions for the role. */
    def disableManagementPermissions(name: String): IO[KeycloakError, ManagementPermission] = {
      val path: Path = Seq(client.realm, "roles", name, "management", "permissions")
      client.put[ManagementPermission](path, ManagementPermission.Enable(false))
    }
  }
}

package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._

import scala.collection.immutable.Seq

class Roles[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) {

  private val clients_path = "clients"
  private val roles_path   = "roles"

  // ------------------------------------------------------------------------------------------------------------------- //
  // -------------------------------------------------- Client Roles --------------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------------------- //
  object ClientLevel {

    // --- CRUD --- //
    def create(clientId: UUID, role: Role.Create): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path)
      client.post[Unit](path, role.copy(clientRole = true))
    }

    def fetch(clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path)
      client.get[List[Role]](path)
    }

    def fetchByName(clientId: UUID, name: String): R[Either[KeycloakError, Role]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name)
      client.get[Role](path)
    }

    def update(clientId: UUID, name: String, role: Role.Update): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name)
      client.put[Unit](path, role)
    }

    def remove(clientId: UUID, name: String): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name)
      client.delete[Unit](path)
    }

    def fetchUsers(clientId: UUID, name: String, first: Option[Int], max: Option[Int]): R[Either[KeycloakError, List[User]]] = {
      val path: Path  = Seq(realm, clients_path, clientId, roles_path, name, "users")
      val query = createQuery(("first", first), ("max", max))
      client.get[List[User]](path, query = query)
    }

    def fetchGroups(clientId: UUID, name: String, first: Option[Int], max: Option[Int], full: Option[Boolean]): R[Either[KeycloakError, List[Group]]] = {
      val path: Path  = Seq(realm, clients_path, clientId, roles_path, name, "groups")
      val query = createQuery(("first", first), ("max", max), ("full", full))
      client.get[List[Group]](path, query)
    }

    // --- Composites --- //
    def addCompositeRoles(clientId: UUID, name: String, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
      val body = roleIds.map(r => Role.Mapping(Some(r)))
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "composites")
      client.post[Unit](path, body)
    }

    def fetchCompositeRoles(clientId: UUID, name: String): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "composites")
      client.get[List[Role]](path)
    }

    def removeCompositeRoles(clientId: UUID, name: String, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
      val body = roleIds.map(r => Role.Mapping(Some(r)))
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "composites")
      client.delete[Unit](path, body)
    }

    def fetchClientCompositeRoles(clientId: UUID, name: String, compositeClientId: UUID): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "composites", "clients", compositeClientId)
      client.get[List[Role]](path)
    }

    def fetchRealmCompositeRoles(clientId: UUID, name: String): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "composites", "realm")
      client.get[List[Role]](path)
    }

    // --- Permissions --- //
    def authPermissionsInitialised(clientId: UUID, name: String): R[Either[KeycloakError, ManagementPermission]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "management", "permissions")
      client.get[ManagementPermission](path)
    }

    def initialiseAuthPermissions(clientId: UUID, name: String, ref: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
      val path: Path = Seq(realm, clients_path, clientId, roles_path, name, "management", "permissions")
      client.put[ManagementPermission](path, ref)
    }
  }

  // ------------------------------------------------------------------------------------------------------------------ //
  // -------------------------------------------------- Realm Roles --------------------------------------------------- //
  // ------------------------------------------------------------------------------------------------------------------ //
  object RealmLevel {

    // --- CRUD --- //
    def create(role: Role.Create): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, roles_path)
      client.post[Unit](path, role.copy(clientRole = false))
    }

    def fetch(): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, roles_path)
      client.get[List[Role]](path)
    }

    def fetchByName(name: String): R[Either[KeycloakError, Role]] = {
      val path: Path = Seq(realm, roles_path, name)
      client.get[Role](path)
    }

    def update(name: String, role: Role.Update): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, roles_path, name)
      client.put[Unit](path, role)
    }

    def remove(name: String): R[Either[KeycloakError, Unit]] = {
      val path: Path = Seq(realm, roles_path, name)
      client.delete[Unit](path)
    }

    def fetchUsers(name: String, first: Option[Int], max: Option[Int]): R[Either[KeycloakError, List[User]]] = {
      val path: Path = Seq(realm, roles_path, name, "users")
      val query = createQuery(("first", first), ("max", max))
      client.get[List[User]](path, query = query)
    }

    def fetchGroups(name: String, first: Option[Int], max: Option[Int], full: Option[Boolean]): R[Either[KeycloakError, List[Group]]] = {
      val path: Path = Seq(realm, roles_path, name, "groups")
      val query = createQuery(("first", first), ("max", max), ("full", full))
      client.get[List[Group]](path, query)
    }

    // --- Composites --- //
    def addCompositeRoles(name: String, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
      val body = roleIds.map(r => Role.Mapping(Some(r)))
      val path: Path = Seq(realm, roles_path, name, "composites")
      client.post[Unit](path, body)
    }

    def removeCompositeRoles(name: String, roleIds: List[UUID]): R[Either[KeycloakError, Unit]] = {
      val body = roleIds.map(r => Role.Mapping(Some(r)))
      val path: Path = Seq(realm, roles_path, name, "composites")
      client.delete[Unit](path, body)
    }

    def fetchCompositeRoles(name: String): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, roles_path, name, "composites")
      client.get[List[Role]](path)
    }

    def fetchClientCompositeRoles(name: String, clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, roles_path, name, "composites", "clients", clientId)
      client.get[List[Role]](path)
    }

    def fetchRealmCompositeRoles(name: String): R[Either[KeycloakError, List[Role]]] = {
      val path: Path = Seq(realm, roles_path, name, "composites", "realm")
      client.get[List[Role]](path)
    }

    // --- Permissions --- //
    def authPermissionsInitialised(name: String): R[Either[KeycloakError, ManagementPermission]] = {
      val path: Path = Seq(realm, roles_path, name, "management", "permissions")
      client.get[ManagementPermission](path)
    }

    def initialiseAuthPermissions(name: String, ref: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
      val path: Path = Seq(realm, roles_path, name, "management", "permissions")
      client.put[ManagementPermission](path, ref)
    }
  }
}

package com.fullfacing.keycloak4s.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{KeycloakError, ManagementPermission, Role, User}

import scala.collection.immutable.Seq

class Roles[R[+_]: Concurrent, S](realm: String)(implicit keyCloakClient: KeycloakClient[R, S]) {

  private val clients_path = "clients"
  private val roles_path   = "roles"

  // ------------------------------------------------------------------------------------------------------------------- //
  // ------------------------------------------------ Client Roles CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------------------- //
  def createClientRole(clientId: UUID, role: Role): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path)
    keyCloakClient.post[Role, Unit](path, role)
  }

  def createCompositeClientRoles(clientId: UUID, name: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "composites")
    keyCloakClient.post[List[Role], Unit](path, roles)
  }

  def fetchClientRoles(clientId: UUID): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path)
    keyCloakClient.get[List[Role]](path)
  }

  def fetchClientRoleByName(clientId: UUID, name: String): R[Either[KeycloakError, Role]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name)
    keyCloakClient.get[Role](path)
  }

  def fetchClientCompositeRoles(clientId: UUID, name: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "composites")
    keyCloakClient.get[List[Role]](path)
  }

  def updateClientRoleByName(clientId: UUID, name: String, role: Role): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name)
    keyCloakClient.put[Role, Unit](path, role)
  }

  def removeClientRoleByName(clientId: UUID, name: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name)
    keyCloakClient.delete(path)
  }

  def removeClientCompositeRoles(clientId: UUID, name: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "composites")
    keyCloakClient.delete[List[Role], Unit](path, roles)
  }

  def fetchCompositesAppLevelRoles(clientId: UUID, name: String, client: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "composites", "clients", client)
    keyCloakClient.get[List[Role]](path)
  }

  def fetchCompositesRealmLevelRoles(clientId: UUID, name: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "composites", "realm")
    keyCloakClient.get[List[Role]](path)
  }

  def clientRolePermissions(clientId: UUID, name: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "management", "permissions")
    keyCloakClient.get[ManagementPermission](path)
  }

  def fetchUsersByClientRole(clientId: UUID, name: String, first: Option[Int], max: Option[Int]): R[Either[KeycloakError, List[User]]] = {
    val path = Seq(realm, clients_path, clientId.toString, roles_path, name, "users")
    val query = createQuery(("first", first), ("max", max))
    keyCloakClient.get[List[User]](path, query = query)
  }


  // ------------------------------------------------------------------------------------------------------------------ //
  // ------------------------------------------------ Realm Roles CRUD ------------------------------------------------ //
  // ------------------------------------------------------------------------------------------------------------------ //
  def createRealmRole(role: Role): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, roles_path)
    keyCloakClient.post[Role, Unit](path, role)
  }

  def fetchRealmRoles(): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, roles_path)
    keyCloakClient.get[List[Role]](path)
  }

  def fetchRealmRoleByName(name: String): R[Either[KeycloakError, Role]] = {
    val path = Seq(realm, roles_path, name)
    keyCloakClient.get[Role](path)
  }

  def updateRealmRoleByName(name: String, role: Role): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, roles_path, name)
    keyCloakClient.put[Role, Unit](path, role)
  }

  def removeRealmRoleByName(name: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, roles_path, name)
    keyCloakClient.delete(path)
  }

  def createCompositeRealmRoles(name: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, roles_path, name, "composites")
    keyCloakClient.post[List[Role], Unit](path, roles)
  }

  def fetchCompositeRealmRoles(name: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, roles_path, name, "composites")
    keyCloakClient.get[List[Role]](path)
  }
  
  def removeCompositeRealmRoles(name: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, roles_path, name, "composites")
    keyCloakClient.delete[List[Role], Unit](path, roles)
  }

  def fetchUsersByname(name: String, first: Option[Int], max: Option[Int]): R[Either[KeycloakError, User]] = {
    val path  = Seq(realm, roles_path, name, "users")
    val query = createQuery(("first", first), ("max", max))
    keyCloakClient.get[User](path, query = query)
  }
}
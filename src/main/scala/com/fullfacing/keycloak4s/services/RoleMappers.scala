package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{KeycloakError, Mappings, Role}

import scala.collection.immutable.Seq

class RoleMappers[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  private val groups_path   = "groups"
  private val role_mappings = "role-mappings"

  /**
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def fetchRoleMappings(realm: String, id: String): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(realm, groups_path, id, role_mappings)
    client.get[Mappings](path)
  }

  /**
   * Add realm-level role mappings to the user
   *
   * @param realm realm name (not id!)
   * @param id
   * @param roles Roles to add
   * @return
   */
  def addRealmLevelRoleMappings(realm: String, id: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    client.post[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level role mappings
   *
   * @param realm
   * @param id
   * @return
   */
  def fetchRealmRoleMappings(realm: String, id: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    client.get[List[Role]](path)
  }

  /**
   * Delete realm-level role mappings
   *
   * @param realm realm name (not id!)
   * @param id
   * @param roles Roles to be deleted
   * @return
   */
  def removeRealmRoleMappings(realm: String, id: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    client.delete[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level roles that can be mapped
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def getAvailableRealmRoles(realm: String, id: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm", "available")
    client.get[List[Role]](path)
  }

  /**
   * Get effective realm-level role mappings This will recurse all composite roles to get the result.
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def getEffectiveRealmRoleMappings(realm: String, id: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm", "composite")
    client.get[List[Role]](path)
  }

  /**
   * Get role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getUserRoleMappings(realm: String, userId: String): R[Either[KeycloakError, Mappings]] = {
    val path = Seq(realm, "users", userId, role_mappings)
    client.get[Mappings](path)
  }

  /**
   * Add realm-level role mappings to the user
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @param roles  Roles to add
   * @return
   */
  def addRealmRoleMappingsToUser(realm: String, userId: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    client.post[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getUserRealmRoleMappings(realm: String, userId: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    client.get[List[Role]](path)
  }

  /**
   * Delete realm-level role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @param roles  Roles to be deleted
   * @return
   */
  def removeUserRealmRoleMappings(realm: String, userId: String, roles: List[Role]): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    client.delete[List[Role], Unit](path, roles)
  }

  /**
   * Get realm-level roles that can be mapped
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getAvailableUserRealmRoles(realm: String, userId: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm", "available")
    client.get[List[Role]](path)
  }

  /**
   * Get effective realm-level role mappings. This will recurse all composite roles to get the result.
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getEffectiveUserRealmRoles(realm: String, userId: String): R[Either[KeycloakError, List[Role]]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm", "available")
    client.get[List[Role]](path)
  }
}
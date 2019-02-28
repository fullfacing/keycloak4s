package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.{Mappings, Role}

import scala.collection.immutable.Seq

object RoleMapper {

  private val groups_path   = "groups"
  private val role_mappings = "role-mappings"

  /**
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def fetchRoleMappings(realm: String, id: String): AsyncApolloResponse[Mappings] = {
    val path = Seq(realm, groups_path, id, role_mappings)
    SttpClient.get(path)
  }

  /**
   * Add realm-level role mappings to the user
   *
   * @param realm realm name (not id!)
   * @param id
   * @param roles Roles to add
   * @return
   */
  def addRealmLevelRoleMappings(realm: String, id: String, roles: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    SttpClient.post(roles, path)
  }

  /**
   * Get realm-level role mappings
   *
   * @param realm
   * @param id
   * @return
   */
  def fetchRealmRoleMappings(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    SttpClient.get(path)
  }

  /**
   * Delete realm-level role mappings
   *
   * @param realm realm name (not id!)
   * @param id
   * @param roles Roles to be deleted
   * @return
   */
  def removeRealmRoleMappings(realm: String, id: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm")
    SttpClient.delete(roles, path)
  }

  /**
   * Get realm-level roles that can be mapped
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def getAvailableRealmRoles(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm", "available")
    SttpClient.get(path)
  }

  /**
   * Get effective realm-level role mappings This will recurse all composite roles to get the result.
   *
   * @param realm realm name (not id!)
   * @param id
   * @return
   */
  def getEffectiveRealmRoleMappings(realm: String, id: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, groups_path, id, role_mappings, "realm", "composite")
    SttpClient.get(path)
  }

  /**
   * Get role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getUserRoleMappings(realm: String, userId: String): AsyncApolloResponse[Mappings] = {
    val path = Seq(realm, "users", userId, role_mappings)
    SttpClient.get(path)
  }

  /**
   * Add realm-level role mappings to the user
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @param roles  Roles to add
   * @return
   */
  def addRealmRoleMappingsToUser(realm: String, userId: String, roles: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    SttpClient.post(roles, path)
  }

  /**
   * Get realm-level role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getUserRealmRoleMappings(realm: String, userId: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    SttpClient.get(path)
  }

  /**
   * Delete realm-level role mappings
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @param roles  Roles to be deleted
   * @return
   */
  def removeUserRealmRoleMappings(realm: String, userId: String, roles: List[Role]): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm")
    SttpClient.delete(roles, path)
  }

  /**
   * Get realm-level roles that can be mapped
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getAvailableUserRealmRoles(realm: String, userId: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm", "available")
    SttpClient.get(path)
  }

  /**
   * Get effective realm-level role mappings. This will recurse all composite roles to get the result.
   *
   * @param realm  realm name (not id!)
   * @param userId User id
   * @return
   */
  def getEffectiveUserRealmRoles(realm: String, userId: String): AsyncApolloResponse[List[Role]] = {
    val path = Seq(realm, "users", userId, role_mappings, "realm", "available")
    SttpClient.get(path)
  }
}

package com.fullfacing.keycloak4s.admin.services

import java.io.File

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.models._
import com.fullfacing.keycloak4s.core.models.KeycloakError

import scala.collection.immutable.Seq

class IdentityProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Import identity provider from uploaded JSON file
   *
   * @param config
   * @return
   */
  def importIdentityProvider(config: File): R[Either[KeycloakError, Map[String, String]]] = {
    val path = Seq(client.realm, "identity-provider", "import-config")
    val multipart = createMultipart(config)
    client.post[Map[String, String]](path, multipart)
  }

  /**
   * Create a new identity provider.
   *
   * @param identityProvider  Object representing IdentityProvider details.
   * @return
   */
  def createIdentityProvider(identityProvider: IdentityProvider): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "identity-provider", "instances")
    client.post[Unit](path, identityProvider)
  }

  /**
   * Get identity providers.
   */
  def getIdentityProviders(): R[Either[KeycloakError, Seq[IdentityProvider]]] = {
    val path = Seq(client.realm, "identity-provider", "instances")
    client.get[Seq[IdentityProvider]](path)
  }

  /**
   * Get an identity provider.
   *
   * @param alias
   * @return
   */
  def getIdentityProvider(alias: String): R[Either[KeycloakError, IdentityProvider]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias)
    client.get[IdentityProvider](path)
  }

  /**
   * Update an identity provider.
   *
   * @param alias
   * @return
   */
  def updateIdentityProvider(alias: String, identityProvider: IdentityProvider): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias)
    client.put[Unit](path, identityProvider)
  }

  /**
   * Deletes an identity provider.
   *
   * @param alias
   * @return
   */
  def deleteIdentityProvider(alias: String): R[Either[KeycloakError, IdentityProvider]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias)
    client.delete[IdentityProvider](path)
  }

  /**
   * Export public broker configuration for identity provider.
   *
   * @param alias
   * @param format  Optional format to use.
   * @return
   */
  def exportIdentityProviderBrokerConfig(alias: String, format: Option[String] = None): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("format", format))

    val path = Seq(client.realm, "identity-provider", "instances", alias, "export")
    client.get[Unit](path, query = query)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param alias ID of the group.
   * @return
   */
  def getManagementPermissions(alias: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Update group management permissions .
   *
   * @param alias       ID of the group.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(alias: String, permissions: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "management", "permissions")
    client.put[ManagementPermission](path, permissions)
  }

  /**
   * Get mapper types for identity provider.
   *
   * @param alias
   * @return Map of provider Ids and corresponding IdentityProviderMapper object
   */
  def getMapperTypes(alias: String): R[Either[KeycloakError, Map[String, IdentityProviderMapper]]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mapper-types")
    client.get[Map[String, IdentityProviderMapper]](path)
  }

  /**
   * Add a mapper to identity provider.
   *
   * @param alias
   * @param mapper
   * @return
   */
  def addMapperTypes(alias: String, mapper: IdentityProviderMapper): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mapper")
    client.post[Unit](path, mapper)
  }

  /**
   * Get mappers for identity provider.
   *
   * @param alias
   * @return
   */
  def getMappers(alias: String): R[Either[KeycloakError, Seq[IdentityProviderMapper]]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mappers")
    client.get[Seq[IdentityProviderMapper]](path)
  }

  /**
   * Get mapper by ID for identity provider.
   *
   * @param alias
   * @param mapperId
   * @return
   */
  def getMapper(alias: String, mapperId: String): R[Either[KeycloakError, IdentityProviderMapper]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.get[IdentityProviderMapper](path)
  }

  /**
   * Update a mapper for the identity provider.
   *
   * @param alias
   * @param mapperId
   * @param mapper
   * @return
   */
  def updateMapper(alias: String, mapperId: String, mapper: IdentityProviderMapper): R[Either[KeycloakError, IdentityProviderMapper]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.put[IdentityProviderMapper](path, mapper)
  }

  /**
   * Delete mapper for identity provider.
   *
   * @param alias
   * @param mapperId
   * @return
   */
  def deleteMapper(alias: String, mapperId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.delete[Unit](path)
  }

  /**
   * Get identity providers.
   *
   * @param providerId  Provider id
   * @return
   */
  def getIdentityProviders(providerId: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "identity-provider", "providers", providerId)
    client.get[Unit](path)
  }
}

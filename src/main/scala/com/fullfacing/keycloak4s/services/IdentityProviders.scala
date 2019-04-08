package com.fullfacing.keycloak4s.services

import java.io.File

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Multipart

import scala.collection.immutable.Seq

class IdentityProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Import identity provider from uploaded JSON file
   *
   * @param realm   Name of the Realm.
   * @param config
   * @return
   */
  def importIdentityProvider(realm: String, config: File): R[Either[KeycloakError, Map[String, String]]] = {
    val path = Seq(realm, "identity-provider", "import-config")
    val multipart = createMultipart(config)
    client.post[Multipart, Map[String, String]](path, multipart)
  }

  /**
   * Create a new identity provider.
   *
   * @param realm             Name of the Realm.
   * @param identityProvider  Object representing IdentityProvider details.
   * @return
   */
  def createIdentityProvider(realm: String, identityProvider: IdentityProvider): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "identity-provider", "instances")
    client.post[IdentityProvider, Unit](path, identityProvider)
  }

  /**
   * Get identity providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getIdentityProviders(realm: String): R[Either[KeycloakError, Seq[IdentityProvider]]] = {
    val path = Seq(realm, "identity-provider", "instances")
    client.get[Seq[IdentityProvider]](path)
  }

  /**
   * Get an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getIdentityProvider(alias: String, realm: String): R[Either[KeycloakError, IdentityProvider]] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    client.get[IdentityProvider](path)
  }

  /**
   * Update an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def updateIdentityProvider(alias: String, realm: String, identityProvider: IdentityProvider): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    client.put[IdentityProvider, Unit](path, identityProvider)
  }

  /**
   * Deletes an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def deleteIdentityProvider(alias: String, realm: String): R[Either[KeycloakError, IdentityProvider]] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    client.delete[Unit, IdentityProvider](path)
  }

  /**
   * Export public broker configuration for identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param format  Optional format to use.
   * @return
   */
  def exportIdentityProviderBrokerConfig(alias: String, realm: String, format: Option[String] = None): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("format", format))

    val path = Seq(realm, "identity-provider", "instances", alias, "export")
    client.get(path, query = query)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param alias ID of the group.
   * @param realm Name of the Realm.
   * @return
   */
  def getManagementPermissions(alias: String, realm: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /**
   * Update group management permissions .
   *
   * @param alias       ID of the group.
   * @param realm       Name of the Realm.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(alias: String, realm: String, permissions: ManagementPermission): R[Either[KeycloakError, ManagementPermission]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](path, permissions)
  }

  /**
   * Get mapper types for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return Map of provider Ids and corresponding IdentityProviderMapper object
   */
  def getMapperTypes(alias: String, realm: String): R[Either[KeycloakError, Map[String, IdentityProviderMapper]]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper-types")
    client.get[Map[String, IdentityProviderMapper]](path)
  }

  /**
   * Add a mapper to identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param mapper
   * @return
   */
  def addMapperTypes(alias: String, realm: String, mapper: IdentityProviderMapper): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper")
    client.post[IdentityProviderMapper, Unit](path, mapper)
  }

  /**
   * Get mappers for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getMappers(alias: String, realm: String): R[Either[KeycloakError, Seq[IdentityProviderMapper]]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers")
    client.get[Seq[IdentityProviderMapper]](path)
  }

  /**
   * Get mapper by ID for identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def getMapper(alias: String, mapperId: String, realm: String): R[Either[KeycloakError, IdentityProviderMapper]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.get[IdentityProviderMapper](path)
  }

  /**
   * Update a mapper for the identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @param mapper
   * @return
   */
  def updateMapper(alias: String, mapperId: String, realm: String, mapper: IdentityProviderMapper): R[Either[KeycloakError, IdentityProviderMapper]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.put[IdentityProviderMapper, IdentityProviderMapper](path, mapper)
  }

  /**
   * Delete mapper for identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteMapper(alias: String, mapperId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.delete(path)
  }

  /**
   * Get identity providers.
   *
   * @param providerId  Provider id
   * @param realm       Name of the Realm.
   * @return
   */
  def getIdentityProviders(providerId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "identity-provider", "providers", providerId)
    client.get(path)
  }
}

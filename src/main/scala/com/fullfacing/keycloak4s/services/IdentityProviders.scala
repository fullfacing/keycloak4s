package com.fullfacing.keycloak4s.services

import java.io.File

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

class IdentityProviders[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Import identity provider from uploaded JSON file
   *
   * @param realm   Name of the Realm.
   * @param config
   * @param contentType The file's content type.
   * @return
   */
  def importIdentityProvider(realm: String, config: File, contentType: String): R[Map[String, Any]] = { //TODO Determine return type, modify client to handle multipart/form-data
    val path = Seq(realm, "identity-provider", "import-config")
    val multipart = createMultipart(config, contentType)
    client.post(multipart, path)
  }

  /**
   * Create a new identity provider.
   *
   * @param realm             Name of the Realm.
   * @param identityProvider  Object representing IdentityProvider details.
   * @return
   */
  def createIdentityProvider(realm: String, identityProvider: IdentityProvider): R[AnyRef] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances")
    client.post[IdentityProvider, AnyRef](identityProvider, path)
  }

  /**
   * Get identity providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getIdentityProviders(realm: String): R[Seq[IdentityProvider]] = {
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
  def getIdentityProvider(alias: String, realm: String): R[IdentityProvider] = {
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
  def updateIdentityProvider(alias: String, realm: String, identityProvider: IdentityProvider): R[IdentityProvider] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    client.put[IdentityProvider, IdentityProvider](identityProvider, path)
  }

  /**
   * Deletes an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def deleteIdentityProvider(alias: String, realm: String): R[IdentityProvider] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    client.delete[IdentityProvider](path)
  }

  /**
   * Export public broker configuration for identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param format  Optional format to use.
   * @return
   */
  def exportIdentityProviderBrokerConfig(alias: String, realm: String, format: Option[String] = None): R[AnyRef] = { //TODO Determine return type
    val query = createQuery(("format", format))

    val path = Seq(realm, "identity-provider", "instances", alias, "export")
    client.get[AnyRef](path, query)
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param alias ID of the group.
   * @param realm Name of the Realm.
   * @return
   */
  def getManagementPermissions(alias: String, realm: String): R[ManagementPermission] = {
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
  def updateManagementPermissions(alias: String, realm: String, permissions: ManagementPermission): R[ManagementPermission] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "management", "permissions")
    client.put[ManagementPermission, ManagementPermission](permissions, path)
  }

  /**
   * Get mapper types for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getMapperTypes(alias: String, realm: String): R[Map[String, Any]] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper-types")
    client.get[Map[String, Any]](path)
  }

  /**
   * Add a mapper to identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param mapper
   * @return
   */
  def addMapperTypes(alias: String, realm: String, mapper: IdentityProviderMapper): R[AnyRef] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper")
    client.post[IdentityProviderMapper, AnyRef](mapper, path)
  }

  /**
   * Get mappers for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getMappers(alias: String, realm: String): R[Seq[IdentityProviderMapper]] = {
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
  def getMapper(alias: String, mapperId: String, realm: String): R[IdentityProviderMapper] = {
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
  def updateMapper(alias: String, mapperId: String, realm: String, mapper: IdentityProviderMapper): R[IdentityProviderMapper] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.put[IdentityProviderMapper, IdentityProviderMapper](mapper, path)
  }

  /**
   * Delete mapper for identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteMapper(alias: String, mapperId: String, realm: String): R[Unit] = {
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
  def getIdentityProviders(providerId: String, realm: String): R[AnyRef] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "providers", providerId)
    client.get[AnyRef](path)
  }
}

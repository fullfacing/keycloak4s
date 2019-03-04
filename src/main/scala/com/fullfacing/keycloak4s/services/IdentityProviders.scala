package com.fullfacing.keycloak4s.services

import java.io.File

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._
import com.fullfacing.keycloak4s.models.enums.ContentType

import scala.collection.immutable.Seq

object IdentityProviders {

  /**
   * Import identity provider from uploaded JSON file
   *
   * @param realm   Name of the Realm.
   * @param config
   * @param contentType The file's content type.
   * @return
   */
  def importIdentityProvider(realm: String, config: File, contentType: ContentType)(implicit authToken: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type, modify SttpClient to handle multipart/form-data
    val path = Seq(realm, "identity-provider", "import-config")
    val multipart = createMultipart(config, contentType)
    SttpClient.post(multipart, path)
  }

  /**
   * Create a new identity provider.
   *
   * @param realm             Name of the Realm.
   * @param identityProvider  Object representing IdentityProvider details.
   * @return
   */
  def createIdentityProvider(realm: String, identityProvider: IdentityProvider)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances")
    SttpClient.post(identityProvider, path)
  }

  /**
   * Get identity providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getIdentityProviders(realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[IdentityProvider]] = {
    val path = Seq(realm, "identity-provider", "instances")
    SttpClient.get(path)
  }

  /**
   * Get an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getIdentityProvider(alias: String, realm: String)(implicit authToken: String): AsyncApolloResponse[IdentityProvider] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    SttpClient.get(path)
  }

  /**
   * Update an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def updateIdentityProvider(alias: String, realm: String, identityProvider: IdentityProvider)(implicit authToken: String): AsyncApolloResponse[IdentityProvider] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    SttpClient.put(identityProvider, path)
  }

  /**
   * Deletes an identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def deleteIdentityProvider(alias: String, realm: String)(implicit authToken: String): AsyncApolloResponse[IdentityProvider] = {
    val path = Seq(realm, "identity-provider", "instances", alias)
    SttpClient.delete(path)
  }

  /**
   * Export public broker configuration for identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param format  Optional format to use.
   * @return
   */
  def exportIdentityProviderBrokerConfig(alias: String, realm: String, format: Option[String] = None)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type
    val query = createQuery(("format", format))

    val path = Seq(realm, "identity-provider", "instances", alias, "export")
    SttpClient.get(path, query.to[Seq])
  }

  /**
   * Return object stating whether client Authorization permissions have been initialized or not and a reference.
   *
   * @param alias ID of the group.
   * @param realm Name of the Realm.
   * @return
   */
  def getManagementPermissions(alias: String, realm: String)(implicit authToken: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "management", "permissions")
    SttpClient.get(path)
  }

  /**
   * Update group management permissions .
   *
   * @param alias       ID of the group.
   * @param realm       Name of the Realm.
   * @param permissions
   * @return
   */
  def updateManagementPermissions(alias: String, realm: String, permissions: ManagementPermission)(implicit authToken: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "management", "permissions")
    SttpClient.put(permissions, path)
  }

  /**
   * Get mapper types for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getMapperTypes(alias: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper-types")
    SttpClient.get(path)
  }

  /**
   * Add a mapper to identity provider.
   *
   * @param alias
   * @param realm   Name of the Realm.
   * @param mapper
   * @return
   */
  def addMapperTypes(alias: String, realm: String, mapper: IdentityProviderMapper)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "instances", alias, "mapper")
    SttpClient.post(mapper, path)
  }

  /**
   * Get mappers for identity provider.
   *
   * @param alias
   * @param realm Name of the Realm.
   * @return
   */
  def getMappers(alias: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Seq[IdentityProviderMapper]] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers")
    SttpClient.get(path)
  }

  /**
   * Get mapper by ID for identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def getMapper(alias: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[IdentityProviderMapper] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    SttpClient.get(path)
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
  def updateMapper(alias: String, mapperId: String, realm: String, mapper: IdentityProviderMapper)(implicit authToken: String): AsyncApolloResponse[IdentityProviderMapper] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    SttpClient.put(mapper, path)
  }

  /**
   * Delete mapper for identity provider.
   *
   * @param alias
   * @param mapperId
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteMapper(alias: String, mapperId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "identity-provider", "instances", alias, "mappers", mapperId)
    SttpClient.delete(path)
  }

  /**
   * Get identity providers.
   *
   * @param providerId  Provider id
   * @param realm       Name of the Realm.
   * @return
   */
  def getIdentityProviders(providerId: String, realm: String)(implicit authToken: String): AsyncApolloResponse[Any] = { //TODO Determine return type
    val path = Seq(realm, "identity-provider", "providers", providerId)
    SttpClient.get(path)
  }
}

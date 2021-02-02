package com.fullfacing.keycloak4s.admin.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.utils.Service._
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models.enums.ProviderType
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}

import java.io.File
import java.util.UUID
import scala.collection.immutable.Seq

class IdentityProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /** Imports an identity provider from a JSON file. */
  def `import`(config: File): R[Either[KeycloakError, Map[String, String]]] = {
    val path: Path = Seq(client.realm, "identity-provider", "import-config")
    val multipart = createMultipart(config)
    client.post[Map[String, String]](path, multipart)
  }

  /** Creates a new identity provider. */
  def create(identityProvider: IdentityProvider.Create): R[Either[KeycloakError, String]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances")
    Concurrent[R].map(client.post[Headers](path, identityProvider))(extractString)
  }

  /** Retrieves a list of identity providers. */
  def fetch(): R[Either[KeycloakError, Seq[IdentityProvider]]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances")
    client.get[Seq[IdentityProvider]](path)
  }

  /** Retrieves an identity provider by alias. */
  def fetchByAlias(alias: String): R[Either[KeycloakError, IdentityProvider]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias)
    client.get[IdentityProvider](path)
  }

  /** Retrieves a list of identity provider types. */
  def fetchProviderType(providerTypeId: ProviderType): R[Either[KeycloakError, IdentityProvider.Type]] = {
    val path: Path = Seq(client.realm, "identity-provider", "providers", providerTypeId.value)
    client.get[IdentityProvider.Type](path)
  }

  /** Updates an identity provider. */
  def update(alias: String, identityProvider: IdentityProvider.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias)
    client.put[Unit](path, identityProvider)
  }

  /** Deletes an identity provider. */
  def delete(alias: String): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias)
    client.delete[Unit](path)
  }

  /** Exports a public broker configuration for a identity provider. */
  def exportBrokerConfig(alias: String, format: Option[String] = None): R[Either[KeycloakError, Unit]] = {
    val query = createQuery(("format", format))

    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "export")
    client.get[Unit](path, query = query)
  }

  /** Retrieves details of a identity provider's management permissions. */
  def fetchManagementPermissions(alias: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "management", "permissions")
    client.get[ManagementPermission](path)
  }

  /** Enables a identity provider's management permissions. */
  def enableManagementPermissions(alias: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(true))
  }

  /** Disables a identity provider's management permissions. */
  def disableManagementPermissions(alias: String): R[Either[KeycloakError, ManagementPermission]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "management", "permissions")
    client.put[ManagementPermission](path, ManagementPermission.Enable(false))
  }

  /** Retrieves a list of mapper types for a identity provider. */
  def fetchMapperTypes(alias: String): R[Either[KeycloakError, Map[String, IdentityProviderMapperType]]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mapper-types")
    client.get[Map[String, IdentityProviderMapperType]](path)
  }

  /** Adds a mapper to a identity provider. */
  def createMapper(alias: String, mapper: IdentityProviderMapper.Create): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mappers")
    client.post[Unit](path, mapper)
  }

  /** Retrieves a list of mappers for a identity provider. */
  def fetchMappers(alias: String): R[Either[KeycloakError, Seq[IdentityProviderMapper]]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mappers")
    client.get[Seq[IdentityProviderMapper]](path)
  }

  /** Retrieves a mapper by ID for a identity provider. */
  def fetchMapper(alias: String, mapperId: UUID): R[Either[KeycloakError, IdentityProviderMapper]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.get[IdentityProviderMapper](path)
  }

  /** Updates a mapper for a identity provider. */
  def updateMapper(alias: String, mapperId: UUID, mapper: IdentityProviderMapper.Update): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.put[Unit](path, mapper)
  }

  /** Deletes a mapper for a identity provider. */
  def deleteMapper(alias: String, mapperId: UUID): R[Either[KeycloakError, Unit]] = {
    val path: Path = Seq(client.realm, "identity-provider", "instances", alias, "mappers", mapperId)
    client.delete[Unit](path)
  }
}

package com.fullfacing.keycloak4s.admin.monix.bio.services

import java.util.UUID

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}
import monix.bio.IO

import scala.collection.immutable.Seq

class AuthenticationManagement(implicit client: KeycloakClient) {

  /** Retrieves a list of authenticator providers. */
  def fetchAuthenticatorProviders(): IO[KeycloakError, Seq[AuthenticationProvider]] = {
    val path: Path = Seq(client.realm, "authentication", "authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path)
  }

  /** Retrieves a list of client authenticator providers. */
  def fetchClientAuthenticatorProviders(): IO[KeycloakError, Seq[AuthenticationProvider]] = {
    val path: Path = Seq(client.realm, "authentication", "client-authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path)
  }

  /** Retrieves a description of an authenticator provider's configuration. */
  def fetchAuthenticatorProviderConfigDescription(providerId: String): IO[KeycloakError, AuthenticatorConfigInfo] = {
    val path: Path = Seq(client.realm, "authentication", "config-description", providerId)
    client.get[AuthenticatorConfigInfo](path)
  }

  /** Retrieves an authenticator provider's configuration. */
  def fetchAuthenticatorProviderConfig(configId: UUID): IO[KeycloakError, AuthenticatorConfig] = {
    val path: Path = Seq(client.realm, "authentication", "config", configId)
    client.get[AuthenticatorConfig](path)
  }

  /** Replaces an authenticator provider's configuration. */
  def updateAuthenticatorProviderConfig(configId: UUID, replacement: AuthenticatorConfig.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "config", configId)
    client.put[Unit](path, replacement)
  }

  /** Deletes an authenticator provider's configuration. */
  def deleteAuthenticatorProviderConfig(configId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "config", configId)
    client.delete[Unit](path)
  }

  /** Add a new authentication execution. */
  def createExecution(request: AuthenticationExecution): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "executions")
    client.post[Unit](path, request)
  }

  /** Retrieves an authentication execution. */
  def fetchExecution(executionId: UUID): IO[KeycloakError, AuthenticationExecution] = {
    val path: Path = Seq(client.realm, "authentication", "executions", executionId)
    client.get[AuthenticationExecution](path)
  }

  /** Deletes an authentication execution. */
  def deleteExecution(executionId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "executions", executionId)
    client.delete[Unit](path)
  }

  /** Creates a configuration for an authentication execution flow. */
  def createExecutionConfig(executionId: UUID, request: AuthenticatorConfig.Create): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "executions", executionId, "config")
    client.post[Unit](path, request)
  }

  /** Lowers an authentication execution's priority. */
  def lowerExecutionPriority(executionId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "executions", executionId, "lower-priority")
    client.post[Unit](path)
  }

  /** Raises an authentication execution's priority. */
  def raiseExecutionPriority(executionId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "executions", executionId, "raise-priority")
    client.post[Unit](path)
  }

  /** Retrieves a list of authentication flows. */
  def fetchFlows(): IO[KeycloakError, Seq[AuthenticationFlow]] = {
    val path: Path = Seq(client.realm, "authentication", "flows")
    client.get[Seq[AuthenticationFlow]](path)
  }

  /** Retrieves an authentication flow by id. */
  def fetchFlowById(flowId: UUID): IO[KeycloakError, AuthenticationFlow] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowId)
    client.get[AuthenticationFlow](path)
  }

  /** Duplicate an authentication flow under a new name. */
  def copyFlow(flowAlias: String, newName: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowAlias, "copy")
    client.post[Unit](path, Map("newName" -> newName))
  }

  /** Retrieves the executions for an authentication flow. */
  def fetchFlowExecutions(flowAlias: String): IO[KeycloakError, Seq[AuthenticationExecutionInfo]] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.get[Seq[AuthenticationExecutionInfo]](path)
  }

  /** Update the executions of an authentication flow. */
  def updateFlowExecutions(flowAlias: String, request: AuthenticationExecutionInfo): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.put[Unit](path, request)
  }

  /** Add new execution to an authentication flow. */
  def createFlowExecution(flowAlias: String, provider: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "execution")
    client.post[Unit](path, ProviderWrapper(provider))
  }

  /** Adds a new authentication flow to an existing flow. */
  def addNewFlowToExistingFlow(flowAlias: String, request: NewAuthenticationFlow): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "flow")
    client.post[Unit](path, request)
  }

  /** Updates an authentication flow. */
  def updateFlow(flowId: UUID, flow: AuthenticationFlow): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowId)
    client.put[Unit](path, flow)
  }

  /** Deletes an authentication flow. */
  def deleteFlow(flowId: UUID): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "flows", flowId)
    client.delete[Unit](path)
  }

  /** Retrieves a list of form action providers. */
  def fetchFormActionProviders(): IO[KeycloakError, Seq[FormProvider]] = {
    val path: Path = Seq(client.realm, "authentication", "form-action-providers")
    client.get[Seq[FormProvider]](path)
  }

  /** Retrieves a list of form providers. */
  def fetchFormProviders(): IO[KeycloakError, Seq[FormProvider]] = {
    val path: Path = Seq(client.realm, "authentication", "form-providers")
    client.get[Seq[FormProvider]](path)
  }

  /** Retrieves the descriptions of all client configurations. */
  def fetchConfigurationDescriptions(): IO[KeycloakError, Map[String, List[ConfigProperty]]] = {
    val path: Path = Seq(client.realm, "authentication", "per-client-config-description")
    client.get[Map[String, List[ConfigProperty]]](path)
  }

  /** Registers a new required action. */
  def registerRequiredAction(requiredAction: AuthRequiredAction): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "register-required-action")
    client.post[Unit](path, requiredAction)
  }

  /**  Retrieves a list of required actions. */
  def fetchRequiredActions(): IO[KeycloakError, Seq[RequiredActionProvider]] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions")
    client.get[Seq[RequiredActionProvider]](path)
  }

  /** Retrieves a required action by alias. */
  def fetchRequiredActionByAlias(alias: String): IO[KeycloakError, RequiredActionProvider] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions", alias)
    client.get[RequiredActionProvider](path)
  }

  /** Updates a required action. */
  def updateRequiredAction(alias: String, request: RequiredActionProvider.Update): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions", alias)
    client.put[Unit](path, request)
  }

  /** Deletes a required action. */
  def deleteRequiredAction(alias: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions", alias)
    client.delete[Unit](path)
  }

  /** Lowers a required action’s priority. */
  def lowerRequiredActionPriority(alias: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions", alias, "lower-priority")
    client.post[Unit](path)
  }

  /** Raises a required action’s priority. */
  def raiseRequiredActionPriority(alias: String): IO[KeycloakError, Unit] = {
    val path: Path = Seq(client.realm, "authentication", "required-actions", alias, "raise-priority")
    client.post[Unit](path)
  }

  /** Retrieves a list of unregistered required actions. */
  def fetchUnregisteredRequiredActions(): IO[KeycloakError, Seq[Map[String, AnyRef]]] = {
    val path: Path = Seq(client.realm, "authentication", "unregistered-required-actions")
    client.get[Seq[Map[String, AnyRef]]](path)
  }
}

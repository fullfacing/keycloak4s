package com.fullfacing.keycloak4s.admin.services

import java.util.UUID

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, _}

import scala.collection.immutable.Seq

class AuthenticationManagement[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /* Retrieves a list of authenticator providers. **/
  def fetchAuthenticatorProviders(): R[Either[KeycloakError, Seq[AuthenticationProvider]]] = {
    client.get[Seq[AuthenticationProvider]](client.realm :: "authentication" :: "authenticator-providers" :: Nil)
  }

  /* Retrieves a list of client authenticator providers. **/
  def fetchClientAuthenticatorProviders(): R[Either[KeycloakError, Seq[AuthenticationProvider]]] = {
    val path = Seq(client.realm, "authentication", "client-authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path)
  }

  /* Retrieves a description of an authenticator provider's configuration. **/
  def fetchProviderConfigDescription(providerId: String): R[Either[KeycloakError, AuthenticatorConfigInfo]] = {
    val path = Seq(client.realm, "authentication", "config-description", providerId)
    client.get[AuthenticatorConfigInfo](path)
  }

  /* Retrieves an authenticator provider's configuration. **/
  def fetchAuthenticatorConfig(configId: UUID): R[Either[KeycloakError, AuthenticatorConfig]] = {
    val path = Seq(client.realm, "authentication", "config", configId.toString)
    client.get[AuthenticatorConfig](path)
  }

  /* Replaces an authenticator provider's configuration. **/
  def updateAuthenticatorConfig(configId: UUID, replacement: AuthenticatorConfig.Update): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "config", configId.toString)
    client.put[Unit](path, replacement)
  }

  /* Deletes an authenticator provider's configuration. **/
  def deleteAuthenticatorConfig(configId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "config", configId.toString)
    client.delete[Unit](path)
  }

  /* Add a new authentication execution. **/
  def createAuthenticationExecution(request: AuthenticationExecution): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "executions")
    client.post[Unit](path, request)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def fetchSingleExecution(executionId: UUID): R[Either[KeycloakError, AuthenticationExecution]] = {
    val path = Seq(client.realm, "authentication", "executions", executionId.toString)
    client.get[AuthenticationExecution](path)
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def deleteExecution(executionId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "executions", executionId.toString)
    client.delete[Unit](path)
  }

  /**
   * Creates a new authentication config for an execution flow.
   *
   * @param executionId ID of the execution.
   * @param request     Object describing new configuration.
   * @return
   */
  def createExecutionConfig(executionId: UUID, request: AuthenticatorConfig.Create): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "executions", executionId.toString, "config")
    client.post[Unit](path, request)
  }

  /**
   * Lower an execution's priority.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def lowerExecutionPriority(executionId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "executions", executionId.toString, "lower-priority")
    client.post[Unit](path)
  }

  /**
   * Raise an execution's priority.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def raiseExecutionPriority(executionId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "executions", executionId.toString, "raise-priority")
    client.post[Unit](path)
  }

  /**
   * Returns a list of authentication flows.
   *
   * @return
   */
  def fetchAuthenticationFlows(): R[Either[KeycloakError, Seq[AuthenticationFlow]]] = {
    val path = Seq(client.realm, "authentication", "flows")
    client.get[Seq[AuthenticationFlow]](path)
  }

  /**
   * Copy existing authentication flow under a new name.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param newName   The name for the flow copy.
   * @return
   */
  def copyAuthenticationFlow(flowAlias: String, newName: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "copy")
    client.post[Unit](path, Map("newName" -> newName))
  }

  /**
   * Get authentication executions for a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @return
   */
  def fetchFlowAuthenticationExecutions(flowAlias: String): R[Either[KeycloakError, Seq[AuthenticationExecutionInfo]]] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.get[Seq[AuthenticationExecutionInfo]](path)
  }

  /**
   * Update authentication executions of a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param request   Object describing updated authentication executions.
   * @return
   */
  def updateFlowAuthenticationExecutions(flowAlias: String, request: AuthenticationExecutionInfo): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.put[Unit](path, request)
  }

  /**
   * Add new authentication execution to a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param provider  Provider ID
   * @return
   */
  def createFlowAuthenticationExecution(flowAlias: String, provider: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "execution")
    client.post[Unit](path, ProviderWrapper(provider))
  }

  /**
   * Add new flow with new execution to existing flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param request   Object describing new authentication flow.
   * @return
   */
  def createNewFlowWithNewExecution(flowAlias: String, request: NewAuthenticationFlow): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "flow")
    client.post[Unit](path, request)
  }

  /**
   * Get authentication flow with id.
   *
   * @param flowId    ID of an existing authentication flow.
   * @return
   */
  def fetchAuthenticationFlow(flowId: UUID): R[Either[KeycloakError, AuthenticationFlow]] = {
    val path = Seq(client.realm, "authentication", "flows", flowId.toString)
    client.get[AuthenticationFlow](path)
  }

  /**
   * Update an authentication flow.
   *
   * @param flowId ID of an existing authentication flow.
   * @param flow   Authentication flow representation.
   * @return
   */
  def updateAuthenticationFlow(flowId: UUID, flow: AuthenticationFlow): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowId.toString)
    client.put[Unit](path, flow)
  }

  /**
   * Delete an authentication flow.
   *
   * @param flowId    ID of an existing authentication flow.
   * @return
   */
  def deleteAuthenticationFlow(flowId: UUID): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "flows", flowId.toString)
    client.delete[Unit](path)
  }

  /**
   * Returns a list of form action providers.
   *
   * @return
   */
  def fetchFormActionProviders(): R[Either[KeycloakError, Seq[FormProvider]]] = {
    val path = Seq(client.realm, "authentication", "form-action-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Returns a list of form providers.
   *
   * @return
   */
  def fetchFormProviders(): R[Either[KeycloakError, Seq[FormProvider]]] = {
    val path = Seq(client.realm, "authentication", "form-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Get configuration descriptions for all clients.
   *
   * @return Map of the realm's client auth types and their configurations
   */
  def fetchConfigurationDescriptions(): R[Either[KeycloakError, Map[String, List[ConfigProperty]]]] = {
    val path = Seq(client.realm, "authentication", "per-client-config-description")
    client.get[Map[String, List[ConfigProperty]]](path)
  }

  /**
   * Register a new required action.
   *
   * @param requiredAction  Details of the required action
   * @return
   */
  def registerRequiredAction(requiredAction: RequiredAction): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "register-required-action")
    client.post[Unit](path, requiredAction)
  }

  /**
   * Returns a list of required actions.
   *
   * @return
   */
  def fetchRequiredActions(): R[Either[KeycloakError, Seq[RequiredActionProvider]]] = {
    val path = Seq(client.realm, "authentication", "required-actions")
    client.get[Seq[RequiredActionProvider]](path)
  }

  /**
   * Get required action for alias.
   *
   * @param alias Alias of required action.
   * @return
   */
  def fetchRequiredAction(alias: String): R[Either[KeycloakError, RequiredActionProvider]] = {
    val path = Seq(client.realm, "authentication", "required-actions", alias)
    client.get[RequiredActionProvider](path)
  }

  /**
   * Update required action.
   *
   * @param alias   Alias of required action.
   * @param request Object describing new state of required action.
   * @return
   */
  def updateRequiredAction(alias: String, request: RequiredActionProvider.Update): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "required-actions", alias)
    client.put[Unit](path, request)
  }

  /**
   * Delete required action.
   *
   * @param alias Alias of required action.
   * @return
   */
  def deleteRequiredAction(alias: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "required-actions", alias)
    client.delete[Unit](path)
  }

  /**
   * Lower required action’s priority.
   *
   * @param alias Alias of required action.
   * @return
   */
  def lowerRequiredActionPriority(alias: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "required-actions", alias, "lower-priority")
    client.post[Unit](path)
  }

  /**
   * Raise required action’s priority.
   *
   * @param alias Alias of required action.
   * @return
   */
  def raiseRequiredActionPriority(alias: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(client.realm, "authentication", "required-actions", alias, "raise-priority")
    client.post[Unit](path)
  }

  /**
   * Returns a list of unregistered required actions.
   */
  def fetchUnregisteredRequiredActions(): R[Either[KeycloakError, Seq[Map[String, AnyRef]]]] = {
    val path = Seq(client.realm, "authentication", "unregistered-required-actions")
    client.get[Seq[Map[String, AnyRef]]](path)
  }
}

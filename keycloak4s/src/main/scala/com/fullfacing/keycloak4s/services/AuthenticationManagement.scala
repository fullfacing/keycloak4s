package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{AuthenticationProvider, _}

import scala.collection.immutable.Seq

class AuthenticationManagement[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Returns a list of authenticator providers.
   *
   * @return
   */
  def getAuthenticationProviders(): R[Seq[AuthenticationProvider]] = {
    client.get[Seq[AuthenticationProvider]](client.realm :: "authentication" :: "authenticator-providers" :: Nil)
  }

  /**
   * Returns a list of client authenticator providers.
   *
   * @return
   */
  def getClientAuthenticationProviders(): R[Seq[AuthenticationProvider]] = {
    val path = Seq(client.realm, "authentication", "client-authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path)
  }

  /**
   * Get authenticator provider’s configuration description.
   *
   * @param providerId ID of the Provider.
   * @return
   */
  def getProviderConfigDescription(providerId: String): R[AuthenticatorConfigInfo] = {
    val path = Seq(client.realm, "authentication", "config-description", providerId)
    client.get[AuthenticatorConfigInfo](path)
  }

  /**
   * Get authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @return
   */
  def getAuthenticatorConfig(configId: String): R[AuthenticatorConfig] = {
    val path = Seq(client.realm, "authentication", "config", configId)
    client.get[AuthenticatorConfig](path)
  }

  /**
   * Update authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param request  Object describing new state of authenticator configuration.
   * @return
   */
  def updateAuthenticatorConfig(configId: String, request: AuthenticatorConfig): R[Unit] = {
    val path = Seq(client.realm, "authentication", "config", configId)
    client.put[AuthenticatorConfig, Unit](path, request)
  }

  /**
   * Delete authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @return
   */
  def deleteAuthenticatorConfig(configId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "config", configId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Add new authentication execution.
   *
   * @param request Object describing authentication execution.
   * @return
   */
  def addNewAuthenticationExecution(request: AuthenticationExecution): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions")
    client.post[AuthenticationExecution, Unit](path, request)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def getSingleExecution(executionId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions", executionId)
    client.get(path)
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def deleteExecution(executionId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions", executionId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Updates an execution with a new configuration.
   *
   * @param executionId ID of the execution.
   * @param request     Object describing new configuration.
   * @return
   */
  def updateExecutionConfig(executionId: String, request: AuthenticatorConfig): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions", executionId, "config")
    client.post[AuthenticatorConfig, Unit](path, request)
  }

  /**
   * Lower an execution's priority.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def lowerExecutionPriority(executionId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions", executionId, "lower-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Raise an execution's priority.
   *
   * @param executionId ID of the execution.
   * @return
   */
  def raiseExecutionPriority(executionId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "executions", executionId, "raise-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Returns a list of authentication flows.
   *
   * @return
   */
  def getAuthenticationFlows(): R[Seq[AuthenticationFlow]] = {
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
  def copyAuthenticationFlow(flowAlias: String, newName: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "copy")
    client.post[Map[String, String], Unit](path, Map("newName" -> newName))
  }

  /**
   * Get authentication executions for a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @return
   */
  def getFlowAuthenticationExecutions(flowAlias: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.get(path)
  }

  /**
   * Update authentication executions of a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param request   Object describing updated authentication executions.
   * @return
   */
  def updateFlowAuthenticationExecutions(flowAlias: String, request: AuthenticationExecutionInfo): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions")
    client.put[AuthenticationExecutionInfo, Unit](path, request)
  }

  /**
   * Add new authentication execution to a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param provider  Provider ID
   * @return
   */
  def addFlowAuthenticationExecution(flowAlias: String, provider: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "execution")
    client.post[ProviderWrapper, Unit](path, ProviderWrapper(provider))
  }

  /**
   * Add new flow with new execution to existing flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param request   Object describing new authentication flow.
   * @return
   */
  def addNewFlowWithNewExecution(flowAlias: String, request: NewAuthenticationFlow): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowAlias, "executions", "flow")
    client.post[NewAuthenticationFlow, Unit](path, request)
  }

  /**
   * Get authentication flow with id.
   *
   * @param flowId    ID of an existing authentication flow.
   * @return
   */
  def getAuthenticationFlow(flowId: String): R[AuthenticationFlow] = {
    val path = Seq(client.realm, "authentication", "flows", flowId)
    client.get[AuthenticationFlow](path)
  }

  /**
   * Update an authentication flow.
   *
   * @param flowId ID of an existing authentication flow.
   * @param flow   Authentication flow representation.
   * @return
   */
  def updateAuthenticationFlow(flowId: String, flow: AuthenticationFlow): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowId)
    client.put[AuthenticationFlow, Unit](path, flow)
  }

  /**
   * Delete an authentication flow.
   *
   * @param flowId    ID of an existing authentication flow.
   * @return
   */
  def deleteAuthenticationFlow(flowId: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "flows", flowId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Returns a list of form action providers.
   *
   * @return
   */
  def getFormActionProviders(): R[Seq[FormProvider]] = {
    val path = Seq(client.realm, "authentication", "form-action-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Returns a list of form providers.
   *
   * @return
   */
  def getFormProviders(): R[Seq[FormProvider]] = {
    val path = Seq(client.realm, "authentication", "form-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Get configuration descriptions for all clients.
   *
   * @return Map of the realm's client auth types and their configurations
   */
  def getConfigurationDescriptions(): R[Map[String, List[ConfigProperty]]] = {
    val path = Seq(client.realm, "authentication", "per-client-config-description")
    client.get[Map[String, List[ConfigProperty]]](path)
  }

  /**
   * Register a new required action.
   *
   * @param providerId      ID of the Provider.
   * @param requiredAction  Details of the required action
   * @return
   */
  def registerRequiredAction(providerId: String, requiredAction: RequiredAction): R[Unit] = {
    val path = Seq(client.realm, "authentication", "register-required-action")
    client.post[RequiredAction, Unit](path, requiredAction)
  }

  /**
   * Returns a list of required actions.
   *
   * @return
   */
  def getRequiredActions(): R[Seq[RequiredActionProvider]] = {
    val path = Seq(client.realm, "authentication", "register-actions")
    client.get[Seq[RequiredActionProvider]](path)
  }

  /**
   * Get required action for alias.
   *
   * @param alias Alias of required action.
   * @return
   */
  def getRequiredAction(alias: String): R[RequiredActionProvider] = {
    val path = Seq(client.realm, "authentication", "register-actions", alias)
    client.get[RequiredActionProvider](path)
  }

  /**
   * Update required action.
   *
   * @param alias   Alias of required action.
   * @param request Object describing new state of required action.
   * @return
   */
  def updateRequiredAction(alias: String, request: RequiredActionProvider): R[Unit] = {
    val path = Seq(client.realm, "authentication", "register-actions", alias)
    client.put[RequiredActionProvider, Unit](path, request)
  }

  /**
   * Delete required action.
   *
   * @param alias Alias of required action.
   * @return
   */
  def deleteRequiredAction(alias: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "register-actions", alias)
    client.delete[Unit, Unit](path)
  }

  /**
   * Lower required action’s priority.
   *
   * @param alias Alias of required action.
   * @return
   */
  def lowerRequiredActionPriority(alias: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "register-actions", alias, "lower-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Raise required action’s priority.
   *
   * @param alias Alias of required action.
   * @return
   */
  def raiseRequiredActionPriority(alias: String): R[Unit] = {
    val path = Seq(client.realm, "authentication", "register-actions", alias, "raise-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Returns a list of unregistered required actions.
   */
  def getUnregisteredRequiredActions(): R[Seq[Map[String, AnyRef]]] = {
    val path = Seq(client.realm, "authentication", "unregistered-required-actions")
    client.get[Seq[Map[String, AnyRef]]](path)
  }
}

package com.fullfacing.keycloak4s.services

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.client.KeycloakClient
import com.fullfacing.keycloak4s.models.{AuthenticationProvider, _}

import scala.collection.immutable.Seq

class AuthenticationManagement[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) {

  /**
   * Returns a list of authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationProviders(realm: String): R[Either[KeycloakError, Seq[AuthenticationProvider]]] = {
    client.get[Seq[AuthenticationProvider]](realm :: "authentication" :: "authenticator-providers" :: Nil)
  }

  /**
   * Returns a list of client authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getClientAuthenticationProviders(realm: String): R[Either[KeycloakError, Seq[AuthenticationProvider]]] = {
    val path = Seq(realm, "authentication", "client-authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path)
  }

  /**
   * Get authenticator provider’s configuration description.
   *
   * @param providerId ID of the Provider.
   * @param realm Name of the Realm.
   * @return
   */
  def getProviderConfigDescription(providerId: String, realm: String): R[Either[KeycloakError, AuthenticatorConfigInfo]] = {
    val path = Seq(realm, "authentication", "config-description", providerId)
    client.get[AuthenticatorConfigInfo](path)
  }

  /**
   * Get authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticatorConfig(configId: String, realm: String): R[Either[KeycloakError, AuthenticatorConfig]] = {
    val path = Seq(realm, "authentication", "config", configId)
    client.get[AuthenticatorConfig](path)
  }

  /**
   * Update authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @param request  Object describing new state of authenticator configuration.
   * @return
   */
  def updateAuthenticatorConfig(configId: String, realm: String, request: AuthenticatorConfig): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "config", configId)
    client.put[AuthenticatorConfig, Unit](path, request)
  }

  /**
   * Delete authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @return
   */
  def deleteAuthenticatorConfig(configId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "config", configId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Add new authentication execution.
   *
   * @param realm   Name of the Realm.
   * @param request Object describing authentication execution.
   * @return
   */
  def addNewAuthenticationExecution(realm: String, request: AuthenticationExecution): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions")
    client.post[AuthenticationExecution, Unit](path, request)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def getSingleExecution(executionId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId)
    client.get(path)
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteExecution(executionId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Updates an execution with a new configuration.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @param request     Object describing new configuration.
   * @return
   */
  def updateExecutionConfig(executionId: String, realm: String, request: AuthenticatorConfig): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId, "config")
    client.post[AuthenticatorConfig, Unit](path, request)
  }

  /**
   * Lower an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def lowerExecutionPriority(executionId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId, "lower-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Raise an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def raiseExecutionPriority(executionId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId, "raise-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Returns a list of authentication flows.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationFlows(realm: String): R[Either[KeycloakError, Seq[AuthenticationFlow]]] = {
    val path = Seq(realm, "authentication", "flows")
    client.get[Seq[AuthenticationFlow]](path)
  }

  /**
   * Copy existing authentication flow under a new name.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param newName   The name for the flow copy.
   * @return
   */
  def copyAuthenticationFlow(flowAlias: String, realm: String, newName: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "copy")
    client.post[Map[String, String], Unit](path, Map("newName" -> newName))
  }

  /**
   * Get authentication executions for a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getFlowAuthenticationExecutions(flowAlias: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    client.get(path)
  }

  /**
   * Update authentication executions of a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing updated authentication executions.
   * @return
   */
  def updateFlowAuthenticationExecutions(flowAlias: String, realm: String, request: AuthenticationExecutionInfo): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    client.put[AuthenticationExecutionInfo, Unit](path, request)
  }

  /**
   * Add new authentication execution to a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param provider  Provider ID
   * @return
   */
  def addFlowAuthenticationExecution(flowAlias: String, realm: String, provider: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "execution")
    client.post[ProviderWrapper, Unit](path, ProviderWrapper(provider))
  }

  /**
   * Add new flow with new execution to existing flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing new authentication flow.
   * @return
   */
  def addNewFlowWithNewExecution(flowAlias: String, realm: String, request: NewAuthenticationFlow): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "flow")
    client.post[NewAuthenticationFlow, Unit](path, request)
  }

  /**
   * Get authentication flow with id.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getAuthenticationFlow(flowId: String, realm: String): R[Either[KeycloakError, AuthenticationFlow]] = {
    val path = Seq(realm, "authentication", "flows", flowId)
    client.get[AuthenticationFlow](path)
  }

  /**
   * Update an authentication flow.
   *
   * @param flowId ID of an existing authentication flow.
   * @param realm  Name of the Realm.
   * @param flow   Authentication flow representation.
   * @return
   */
  def updateAuthenticationFlow(flowId: String, realm: String, flow: AuthenticationFlow): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowId)
    client.put[AuthenticationFlow, Unit](path, flow)
  }

  /**
   * Delete an authentication flow.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteAuthenticationFlow(flowId: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "flows", flowId)
    client.delete[Unit, Unit](path)
  }

  /**
   * Returns a list of form action providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormActionProviders(realm: String): R[Either[KeycloakError, Seq[FormProvider]]] = {
    val path = Seq(realm, "authentication", "form-action-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Returns a list of form providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormProviders(realm: String): R[Either[KeycloakError, Seq[FormProvider]]] = {
    val path = Seq(realm, "authentication", "form-providers")
    client.get[Seq[FormProvider]](path)
  }

  /**
   * Get configuration descriptions for all clients.
   *
   * @param realm Name of the Realm.
   * @return Map of the realm's client auth types and their configurations
   */
  def getConfigurationDescriptions(realm: String): R[Either[KeycloakError, Map[String, List[ConfigProperty]]]] = {
    val path = Seq(realm, "authentication", "per-client-config-description")
    client.get[Map[String, List[ConfigProperty]]](path)
  }

  /**
   * Register a new required action.
   *
   * @param realm           Name of the Realm.
   * @param requiredAction  Details of the required action
   * @return
   */
  def registerRequiredAction(realm: String, requiredAction: RequiredAction): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "register-required-action")
    client.post[RequiredAction, Unit](path, requiredAction)
  }

  /**
   * Returns a list of required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredActions(realm: String): R[Either[KeycloakError, Seq[RequiredActionProvider]]] = {
    val path = Seq(realm, "authentication", "register-actions")
    client.get[Seq[RequiredActionProvider]](path)
  }

  /**
   * Get required action for alias.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredAction(alias: String, realm: String): R[Either[KeycloakError, RequiredActionProvider]] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.get[RequiredActionProvider](path)
  }

  /**
   * Update required action.
   *
   * @param alias   Alias of required action.
   * @param realm   Name of the Realm.
   * @param request Object describing new state of required action.
   * @return
   */
  def updateRequiredAction(alias: String, realm: String, request: RequiredActionProvider): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.put[RequiredActionProvider, Unit](path, request)
  }

  /**
   * Delete required action.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def deleteRequiredAction(alias: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.delete[Unit, Unit](path)
  }

  /**
   * Lower required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def lowerRequiredActionPriority(alias: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "register-actions", alias, "lower-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Raise required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def raiseRequiredActionPriority(alias: String, realm: String): R[Either[KeycloakError, Unit]] = {
    val path = Seq(realm, "authentication", "register-actions", alias, "raise-priority")
    client.post[Unit, Unit](path)
  }

  /**
   * Returns a list of unregistered required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getUnregisteredRequiredActions(realm: String): R[Either[KeycloakError, Seq[Map[String, AnyRef]]]] = {
    val path = Seq(realm, "authentication", "unregistered-required-actions")
    client.get[Seq[Map[String, AnyRef]]](path)
  }
}

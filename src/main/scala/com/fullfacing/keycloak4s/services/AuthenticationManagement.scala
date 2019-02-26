package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object AuthenticationManagement {

  /**
   * Returns a list of authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationProviders(realm: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "authenticator-providers")
    SttpClient.get(path)
  }

  /**
   * Returns a list of client authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getClientAuthenticationProviders(realm: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "client-authenticator-providers")
    SttpClient.get(path)
  }

  /**
   * Get authenticator provider’s configuration description.
   *
   * @param providerId ID of the Provider.
   * @param realm Name of the Realm.
   * @return
   */
  def getProviderConfigDescription(providerId: String, realm: String): AsyncApolloResponse[AuthenticatorConfigInfo] = {
    val path = Seq(realm, "authentication", "config-description", providerId)
    SttpClient.get(path)
  }

  /**
   * Get authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticatorConfig(configId: String, realm: String): AsyncApolloResponse[AuthenticatorConfig] = {
    val path = Seq(realm, "authentication", "config", configId)
    SttpClient.get(path)
  }

  /**
   * Update authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @param request  Object describing new state of authenticator configuration.
   * @return
   */
  def updateAuthenticatorConfig(configId: String, realm: String, request: AuthenticatorConfig): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "config", configId)
    SttpClient.put(request, path)
  }

  /**
   * Delete authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @return
   */
  def deleteAuthenticatorConfig(configId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "config", configId)
    SttpClient.delete(path)
  }

  /**
   * Add new authentication execution.
   *
   * @param realm   Name of the Realm.
   * @param request Object describing authentication execution.
   * @return
   */
  def addNewAuthenticationExecution(realm: String, request: AuthenticationExecution): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions")
    SttpClient.post(request, path)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def getSingleExecution(executionId: String, realm: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId)
    SttpClient.get(path)
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteExecution(executionId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "executions", executionId)
    SttpClient.delete(path)
  }

  /**
   * Updates an execution with a new configuration.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @param request     Object describing new configuration.
   * @return
   */
  def updateExecutionConfig(executionId: String, realm: String, request: AuthenticatorConfig): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId, "config")
    SttpClient.post(request, path)
  }

  /**
   * Lower an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def lowerExecutionPriority(executionId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "executions", executionId, "lower-priority")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Raise an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def raiseExecutionPriority(executionId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "executions", executionId, "raise-priority")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of authentication flows.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationFlows(realm: String): AsyncApolloResponse[Seq[AuthenticationFlow]] = {
    val path = Seq(realm, "authentication", "flows")
    SttpClient.get(path)
  }

  /**
   * Copy existing authentication flow under a new name.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param newName   The name for the flow copy.
   * @return
   */
  def copyAuthenticationFlow(flowAlias: String, realm: String, newName: String): AsyncApolloResponse[Any] = { //TODO Determine return type, and confirm body
    val path = Seq(realm, "authentication", "flows", flowAlias, "copy")
    SttpClient.post(Map("newName" -> newName), path)
  }

  /**
   * Get authentication executions for a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getFlowAuthenticationExecutions(flowAlias: String, realm: String): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    SttpClient.get(path)
  }

  /**
   * Update authentication executions of a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing updated authentication executions.
   * @return
   */
  def updateFlowAuthenticationExecutions(flowAlias: String, realm: String, request: AuthenticationExecutionInfo): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    SttpClient.put(request, path)
  }

  /**
   * Add new authentication execution to a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param provider  //TODO Determine if provider name or id
   * @return
   */
  def addFlowAuthenticationExecution(flowAlias: String, realm: String, provider: String): AsyncApolloResponse[Any] = { //TODO Determine return type, and confirm body
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "execution")
    SttpClient.post(Map("provider" -> provider), path)
  }

  /**
   * Add new flow with new execution to existing flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing new authentication flow.
   * @return
   */
  def addNewFlowWithNewExecution(flowAlias: String, realm: String, request: NewAuthenticationFlow): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "flow")
    SttpClient.post(request, path)
  }

  /**
   * Get authentication flow with id.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getAuthenticationFlow(flowId: String, realm: String): AsyncApolloResponse[AuthenticationFlow] = {
    val path = Seq(realm, "authentication", "flows", flowId)
    SttpClient.get(path)
  }

  /**
   * Update an authentication flow.
   *
   * @param flowId ID of an existing authentication flow.
   * @param realm  Name of the Realm.
   * @param flow   Authentication flow representation.
   * @return
   */
  def updateAuthenticationFlow(flowId: String, realm: String, flow: AuthenticationFlow): AsyncApolloResponse[Any] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowId)
    SttpClient.put(flow, path)
  }

  /**
   * Delete an authentication flow.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteAuthenticationFlow(flowId: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "flows", flowId)
    SttpClient.delete(path)
  }

  /**
   * Returns a list of form action providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormActionProviders(realm: String): AsyncApolloResponse[Seq[Map[String, Any]]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "form-action-providers")
    SttpClient.get(path)
  }

  /**
   * Returns a list of form providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormProviders(realm: String): AsyncApolloResponse[Seq[Map[String, Any]]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "form-providers")
    SttpClient.get(path)
  }

  /**
   * Get configuration descriptions for all clients.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getConfigurationDescriptions(realm: String): AsyncApolloResponse[Map[String, Any]] = {
    val path = Seq(realm, "authentication", "per-client-config-description")
    SttpClient.get(path)
  }

  /**
   * Register a new required action.
   *
   * @param realm       Name of the Realm.
   * @param providerId  ID of the Provider.
   * @param name        Name of the required action //TODO Confirm.
   * @return
   */
  def registerRequiredAction(realm: String, providerId: String, name: String): AsyncApolloResponse[Map[String, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "register-required-action")
    SttpClient.post(Map("providerId" -> providerId, "name" -> name), path)
  }

  /**
   * Returns a list of required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredActions(realm: String): AsyncApolloResponse[Seq[RequiredActionProvider]] = {
    val path = Seq(realm, "authentication", "register-actions")
    SttpClient.get(path)
  }

  /**
   * Get required action for alias.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredAction(alias: String, realm: String): AsyncApolloResponse[RequiredActionProvider] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    SttpClient.get(path)
  }

  /**
   * Update required action.
   *
   * @param alias   Alias of required action.
   * @param realm   Name of the Realm.
   * @param request Object describing new state of required action.
   * @return
   */
  def updateRequiredAction(alias: String, realm: String, request: RequiredActionProvider): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    SttpClient.put(request, path)
  }

  /**
   * Delete required action.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def deleteRequiredAction(alias: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "register-actions", alias)
    SttpClient.delete(path)
  }

  /**
   * Lower required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def lowerRequiredActionPriority(alias: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "register-actions", alias, "lower-priority")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Raise required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def raiseRequiredActionPriority(alias: String, realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "authentication", "register-actions", alias, "raise-priority")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of unregistered required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getUnregisteredRequiredActions(realm: String): AsyncApolloResponse[Seq[Map[String, Any]]] = {
    val path = Seq(realm, "authentication", "unregistered-required-actions")
    SttpClient.get(path)
  }
}

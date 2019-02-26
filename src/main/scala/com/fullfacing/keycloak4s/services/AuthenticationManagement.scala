package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models.{AuthenticationExecution, AuthenticatorConfig, AuthenticatorConfigInfo}
import monix.eval.Task

import scala.collection.immutable.Seq

object AuthenticationManagement {

  /**
   * Returns a list of authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationProviders(realm: String): Task[Either[ErrorPayload, Map[String, Any]]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "authenticator-providers")
    SttpClient.get[Map[String, Any]](path)
  }

  /**
   * Returns a list of client authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getClientAuthenticationProviders(realm: String): Task[Either[ErrorPayload, Map[String, Any]]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "client-authenticator-providers")
    SttpClient.get[Map[String, Any]](path)
  }

  /**
   * Get authenticator provider’s configuration description.
   *
   * @param providerId ID of the Provider.
   * @param realm Name of the Realm.
   * @return
   */
  def getProviderConfigDescription(providerId: String, realm: String): Task[Either[ErrorPayload, AuthenticatorConfigInfo]] = {
    val path = Seq(realm, "authentication", "config-description", providerId)
    SttpClient.get[AuthenticatorConfigInfo](path)
  }

  /**
   * Get authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticatorConfig(configId: String, realm: String): Task[Either[ErrorPayload, AuthenticatorConfig]] = {
    val path = Seq(realm, "authentication", "config", configId)
    SttpClient.get[AuthenticatorConfig](path)
  }

  /**
   * Update authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @param request  Object describing new state of authenticator configuration.
   * @return
   */
  def updateAuthenticatorConfig(configId: String, realm: String, request: AuthenticatorConfig): Task[Either[ErrorPayload, Unit]] = {
    val path = Seq(realm, "authentication", "config", configId)
    SttpClient.put[AuthenticatorConfig, Any](request, path).map(_.map(_ => ()))
  }

  /**
   * Delete authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @return
   */
  def deleteAuthenticatorConfig(configId: String, realm: String): Task[Either[ErrorPayload, Unit]] = {
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
  def addNewAuthenticationExecution(realm: String, request: AuthenticationExecution): Task[Either[ErrorPayload, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions")
    SttpClient.post[AuthenticationExecution, Any](request, path)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def getSingleExecution(executionId: String, realm: String): Task[Either[ErrorPayload, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId)
    SttpClient.get[Any](path)
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteExecution(executionId: String, realm: String): Task[Either[ErrorPayload, Unit]] = {
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
  def updateExecutionConfig(executionId: String, realm: String, request: AuthenticatorConfig): Task[Either[ErrorPayload, Any]] = { //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId, "config")
    SttpClient.post[AuthenticatorConfig, Any](request, path)
  }

  def lowerExecutionPriority(executionId: String, realm: String): Task[Either[ErrorPayload, Unit]] = {
    val path = Seq(realm, "authentication", "executions", executionId, "lower-priority")
    Sttp
  }
}

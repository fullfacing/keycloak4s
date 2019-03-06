package com.fullfacing.keycloak4s.services

import cats.data.Kleisli
import com.fullfacing.keycloak4s.handles.KeycloakClient.Request
import com.fullfacing.keycloak4s.models.{AuthenticationProvider, _}
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object AuthenticationManagement {

  /**
   * Returns a list of authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationProviders[R[_], S](realm: String): Request[R, S, Seq[AuthenticationProvider]] = Kleisli { client =>
    client.get[Seq[AuthenticationProvider]](realm :: "authentication" :: "authenticator-providers" :: Nil, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of client authenticator providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getClientAuthenticationProviders[R[_], S](realm: String): Request[R, S, Seq[AuthenticationProvider]] = Kleisli { client =>
    val path = Seq(realm, "authentication", "client-authenticator-providers")
    client.get[Seq[AuthenticationProvider]](path, Seq.empty[KeyValue])
  }

  /**
   * Get authenticator provider’s configuration description.
   *
   * @param providerId ID of the Provider.
   * @param realm Name of the Realm.
   * @return
   */
  def getProviderConfigDescription[R[_], S](providerId: String, realm: String): Request[R, S, AuthenticatorConfigInfo] = Kleisli { client =>
    val path = Seq(realm, "authentication", "config-description", providerId)
    client.get(path, Seq.empty[KeyValue])
  }

  /**
   * Get authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticatorConfig[R[_], S](configId: String, realm: String): Request[R, S, AuthenticatorConfig] = Kleisli { client =>
    val path = Seq(realm, "authentication", "config", configId)
    client.get(path, Seq.empty[KeyValue])
  }

  /**
   * Update authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @param request  Object describing new state of authenticator configuration.
   * @return
   */
  def updateAuthenticatorConfig[R[_], S](configId: String, realm: String, request: AuthenticatorConfig): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "config", configId)
    client.putNoContent[AuthenticatorConfig](request, path, Seq.empty[KeyValue])
  }

  /**
   * Delete authenticator configuration.
   *
   * @param configId ID of the Configuration.
   * @param realm    Name of the Realm.
   * @return
   */
  def deleteAuthenticatorConfig[R[_], S](configId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "config", configId)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Add new authentication execution.
   *
   * @param realm   Name of the Realm.
   * @param request Object describing authentication execution.
   * @return
   */
  def addNewAuthenticationExecution[R[_], S](realm: String, request: AuthenticationExecution): Request[R, S, AnyRef] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions")
    client.post[AuthenticationExecution, AnyRef](request, path)
  }

  /**
   * Get a single execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def getSingleExecution[R[_], S](executionId: String, realm: String): Request[R, S, Any] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId)
    client.get[Any](path, Seq.empty[KeyValue])
  }

  /**
   * Delete an execution.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def deleteExecution[R[_], S](executionId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "executions", executionId)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Updates an execution with a new configuration.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @param request     Object describing new configuration.
   * @return
   */
  def updateExecutionConfig[R[_], S](executionId: String, realm: String, request: AuthenticatorConfig): Request[R, S, Any] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "executions", executionId, "config")
    client.post[AuthenticatorConfig, Any](request, path)
  }

  /**
   * Lower an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def lowerExecutionPriority[R[_], S](executionId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "executions", executionId, "lower-priority")
    client.post(path)
  }

  /**
   * Raise an execution's priority.
   *
   * @param executionId ID of the execution.
   * @param realm       Name of the Realm.
   * @return
   */
  def raiseExecutionPriority[R[_], S](executionId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "executions", executionId, "raise-priority")
    client.post(path)
  }

  /**
   * Returns a list of authentication flows.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getAuthenticationFlows[R[_], S](realm: String): Request[R, S, Seq[AuthenticationFlow]] = Kleisli { client =>
    val path = Seq(realm, "authentication", "flows")
    client.get[Seq[AuthenticationFlow]](path, Seq.empty[KeyValue])
  }

  /**
   * Copy existing authentication flow under a new name.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param newName   The name for the flow copy.
   * @return
   */
  def copyAuthenticationFlow[R[_], S](flowAlias: String, realm: String, newName: String): Request[R, S, Any] = Kleisli { client => //TODO Determine return type, and confirm body
    val path = Seq(realm, "authentication", "flows", flowAlias, "copy")
    client.post[Any](Map("newName" -> newName), path)
  }

  /**
   * Get authentication executions for a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getFlowAuthenticationExecutions[R[_], S](flowAlias: String, realm: String): Request[R, S, Any] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    client.get[Any](path, Seq.empty[KeyValue])
  }

  /**
   * Update authentication executions of a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing updated authentication executions.
   * @return
   */
  def updateFlowAuthenticationExecutions[R[_], S](flowAlias: String, realm: String, request: AuthenticationExecutionInfo): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions")
    client.putNoContent[AuthenticationExecutionInfo](request, path, Seq.empty[KeyValue])
  }

  /**
   * Add new authentication execution to a flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param provider  //TODO Determine if provider name or id
   * @return
   */
  def addFlowAuthenticationExecution[R[_], S](flowAlias: String, realm: String, provider: String): Request[R, S, Any] = Kleisli { client => //TODO Determine return type, and confirm body
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "execution")
    client.post[Any](Map("provider" -> provider), path)
  }

  /**
   * Add new flow with new execution to existing flow.
   *
   * @param flowAlias Name of the existing authentication flow.
   * @param realm     Name of the Realm.
   * @param request   Object describing new authentication flow.
   * @return
   */
  def addNewFlowWithNewExecution[R[_], S](flowAlias: String, realm: String, request: NewAuthenticationFlow): Request[R, S, Any] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowAlias, "executions", "flow")
    client.post[NewAuthenticationFlow, Any](request, path)
  }

  /**
   * Get authentication flow with id.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def getAuthenticationFlow[R[_], S](flowId: String, realm: String): Request[R, S, AuthenticationFlow] = Kleisli { client =>
    val path = Seq(realm, "authentication", "flows", flowId)
    client.get[AuthenticationFlow](path, Seq.empty[KeyValue])
  }

  /**
   * Update an authentication flow.
   *
   * @param flowId ID of an existing authentication flow.
   * @param realm  Name of the Realm.
   * @param flow   Authentication flow representation.
   * @return
   */
  def updateAuthenticationFlow[R[_], S](flowId: String, realm: String, flow: AuthenticationFlow): Request[R, S, Any] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "flows", flowId)
    client.put[AuthenticationFlow, Any](flow, path, Seq.empty[KeyValue])
  }

  /**
   * Delete an authentication flow.
   *
   * @param flowId    ID of an existing authentication flow.
   * @param realm     Name of the Realm.
   * @return
   */
  def deleteAuthenticationFlow[R[_], S](flowId: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "flows", flowId)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of form action providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormActionProviders[R[_], S](realm: String): Request[R, S, Seq[Map[String, Any]]] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "form-action-providers")
    client.get[Seq[Map[String, Any]]](path, Seq.empty[KeyValue])
  }

  /**
   * Returns a list of form providers.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getFormProviders[R[_], S](realm: String): Request[R, S, Seq[Map[String, Any]]] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "form-providers")
    client.get[Seq[Map[String, Any]]](path, Seq.empty[KeyValue])
  }

  /**
   * Get configuration descriptions for all clients.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getConfigurationDescriptions[R[_], S](realm: String): Request[R, S, Seq[Map[String, Any]]] = Kleisli { client =>
    val path = Seq(realm, "authentication", "per-client-config-description")
    client.get[Seq[Map[String, Any]]](path, Seq.empty[KeyValue])
  }

  /**
   * Register a new required action.
   *
   * @param realm       Name of the Realm.
   * @param providerId  ID of the Provider.
   * @param name        Name of the required action //TODO Confirm.
   * @return
   */
  def registerRequiredAction[R[_], S](realm: String, providerId: String, name: String): Request[R, S, Unit] = Kleisli { client => //TODO Determine return type.
    val path = Seq(realm, "authentication", "register-required-action")
    client.post[Unit](Map("providerId" -> providerId, "name" -> name), path)
  }

  /**
   * Returns a list of required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredActions[R[_], S](realm: String): Request[R, S, Seq[RequiredActionProvider]] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions")
    client.get[Seq[RequiredActionProvider]](path, Seq.empty[KeyValue])
  }

  /**
   * Get required action for alias.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def getRequiredAction[R[_], S](alias: String, realm: String): Request[R, S, RequiredActionProvider] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.get[RequiredActionProvider](path, Seq.empty[KeyValue])
  }

  /**
   * Update required action.
   *
   * @param alias   Alias of required action.
   * @param realm   Name of the Realm.
   * @param request Object describing new state of required action.
   * @return
   */
  def updateRequiredAction[R[_], S](alias: String, realm: String, request: RequiredActionProvider): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.putNoContent[RequiredActionProvider](request, path, Seq.empty[KeyValue])
  }

  /**
   * Delete required action.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def deleteRequiredAction[R[_], S](alias: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions", alias)
    client.delete(path, Seq.empty[KeyValue])
  }

  /**
   * Lower required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def lowerRequiredActionPriority[R[_], S](alias: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions", alias, "lower-priority")
    client.post(path)
  }

  /**
   * Raise required action’s priority.
   *
   * @param alias Alias of required action.
   * @param realm Name of the Realm.
   * @return
   */
  def raiseRequiredActionPriority[R[_], S](alias: String, realm: String): Request[R, S, Unit] = Kleisli { client =>
    val path = Seq(realm, "authentication", "register-actions", alias, "raise-priority")
    client.post(path)
  }

  /**
   * Returns a list of unregistered required actions.
   *
   * @param realm Name of the Realm.
   * @return
   */
  def getUnregisteredRequiredActions[R[_], S](realm: String): Request[R, S, Seq[Map[String, Any]]] = Kleisli { client =>
    val path = Seq(realm, "authentication", "unregistered-required-actions")
    client.get[Seq[Map[String, Any]]](path, Seq.empty[KeyValue])
  }
}

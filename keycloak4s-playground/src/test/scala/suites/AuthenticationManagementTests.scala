package suites

import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import com.fullfacing.keycloak4s.core.models._
import monix.eval.Task
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class AuthenticationManagementTests extends IntegrationSpec {

  /**
   * Calls in the Authentication Management service not covered by the below tests:
   *
   * fetchUnregisteredRequiredActions   - It is unclear how to unregister an action, and with no default unregistered actions serialization cannot be tested.
   * createAuthenticationExecution      - Creating or modifying authentication flows or executions involves internal business logic that is not made clear, resulting in
   *                                      frequent 400 Bad Request and 500 Internal Server Error responses, and can even break the Keycloak interface entirely.
   * updateFlowAuthenticationExecutions - See createAuthenticationExecution.
   * createFlowAuthenticationExecution  - See createAuthenticationExecution.
   * createNewFlowWithNewExecution      - See createAuthenticationExecution.
   * updateAuthenticationFlow           - See createAuthenticationExecution.
   */

  val storedExecutionsInfo: AtomicReference[Seq[AuthenticationExecutionInfo]] = new AtomicReference[Seq[AuthenticationExecutionInfo]]()
  val storedExecutionInfo: AtomicReference[AuthenticationExecutionInfo] = new AtomicReference[AuthenticationExecutionInfo]()
  val storedExecution: AtomicReference[AuthenticationExecution] = new AtomicReference[AuthenticationExecution]()
  val storedConfig: AtomicReference[AuthenticatorConfig] = new AtomicReference[AuthenticatorConfig]()
  val storedAction: AtomicReference[RequiredActionProvider] = new AtomicReference[RequiredActionProvider]()
  val storedFlow: AtomicReference[AuthenticationFlow] = new AtomicReference[AuthenticationFlow]()

  "fetchAuthenticationProviders" should "retrieve a non-empty list of Authentication Providers" in {
    authMgmt.fetchAuthenticatorProviders().map(_.map { providers =>
      providers shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchClientAuthenticationProviders" should "retrieve a non-empty list of Client Authentication Providers" in {
    authMgmt.fetchClientAuthenticatorProviders().map(_.map { providers =>
      providers shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchProviderConfigDescription" should "retrieve information of a specified Authentication Provider" in {
    authMgmt.fetchProviderConfigDescription("client-secret").map(_.map { info =>
      info.name shouldBe "Client Id and Secret"
    })
  }.shouldReturnSuccess

  "fetchFlowAuthenticationExecutions" should "retrieve a non-empty list of a specified Flow's Authentication Executions" in {
    authMgmt.fetchFlowAuthenticationExecutions("browser").map(_.map { executions =>
      executions shouldNot be (empty)
      storedExecutionsInfo.set(executions)
    })
  }.shouldReturnSuccess

  "createExecutionConfig" should "create a configuration for an Execution" in {
    val config = AuthenticatorConfig.Create(alias = "test_config")
    val option = storedExecutionsInfo.get().find(_.configurable)

    for {
      execution <- EitherT.fromOption[Task](option, Errors.EXECUTION_NOT_FOUND)
      _         <- EitherT(authMgmt.createExecutionConfig(execution.id, config))
    } yield storedExecutionInfo.set(execution)
  }.value.shouldReturnSuccess

  "fetchSingleExecution" should "retrieve a specified Execution" in {
    authMgmt.fetchSingleExecution(storedExecutionInfo.get().id).map(_.map { execution =>
      storedExecution.set(execution)
    })
  }.shouldReturnSuccess

  "fetchAuthenticatorConfig" should "retrieve a specified Authentication configuration" in {
    val option = storedExecution.get().authenticatorConfig

    for {
      configId  <- EitherT.fromOption[Task](option, Errors.CONFIG_NOT_FOUND)
      config    <- EitherT(authMgmt.fetchAuthenticatorConfig(configId))
    } yield {
      config.alias shouldBe "test_config"
      storedConfig.set(config)
    }
  }.value.shouldReturnSuccess

  "updateAuthenticatorConfig" should "update a specified Authentication configuration" in {
    val update = AuthenticatorConfig.Update(alias = Some("test_config2"))

    for {
      _       <- EitherT(authMgmt.updateAuthenticatorConfig(storedConfig.get().id, update))
      config  <- EitherT(authMgmt.fetchAuthenticatorConfig(storedConfig.get().id))
    } yield config.alias shouldBe "test_config2"
  }.value.shouldReturnSuccess

  "deleteAuthenticatorConfig" should "delete a specified Authentication configuration" in {
    authMgmt.deleteAuthenticatorConfig(storedConfig.get().id)
  }.shouldReturnSuccess

  "copyAuthenticationFlow" should "copy an existing Authentication flow under a new name" in {
    authMgmt.copyAuthenticationFlow("browser", "temp_flow")
  }.shouldReturnSuccess

  "lowerExecutionPriority" should "lower the priority of an Execution" in {

    for {
      infos       <- EitherT(authMgmt.fetchFlowAuthenticationExecutions("temp_flow"))
      info        <- EitherT.fromOption[Task](infos.headOption, Errors.EXECUTION_NOT_FOUND)
      execution1  <- EitherT(authMgmt.fetchSingleExecution(info.id))
      _           <- EitherT(authMgmt.lowerExecutionPriority(execution1.id))
      execution2  <- EitherT(authMgmt.fetchSingleExecution(info.id))
    } yield {
      execution2.priority should be > execution1.priority
      storedExecutionsInfo.set(infos)
      storedExecutionInfo.set(info)
      storedExecution.set(execution2)
    }
  }.value.shouldReturnSuccess

  "raiseExecutionPriority" should "raise the priority of an Execution" in {
    val execution1 = storedExecution.get()

    for {
      _           <- EitherT(authMgmt.raiseExecutionPriority(execution1.id))
      execution2  <- EitherT(authMgmt.fetchSingleExecution(execution1.id))
    } yield execution2.priority should be < execution1.priority
  }.value.shouldReturnSuccess

  "deleteExecution" should "delete a specified Execution" in {
    authMgmt.deleteExecution(storedExecution.get().id)
  }.shouldReturnSuccess

  "fetchFormActionProviders" should "retrieve a non-empty list of Form Action Providers" in {
    authMgmt.fetchFormActionProviders().map(_.map { providers =>
      providers shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchFormProviders" should "retrieve a non-empty list of Form Providers" in {
    authMgmt.fetchFormProviders().map(_.map { providers =>
      providers shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchConfigurationDescriptions" should "retrieve a non-empty list of Configuration Descriptions" in {
    authMgmt.fetchConfigurationDescriptions().map(_.map { descriptions =>
      descriptions shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchRequiredActions" should "retrieve a non-empty list of Required Actions" in {
    authMgmt.fetchRequiredActions().map(_.map { actions =>
      actions shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "registerRequiredAction" should "register a Required Action" in {
    val action = RequiredAction(name = "test_action", providerId = "test_action_provider")

    authMgmt.registerRequiredAction(action)
  }.shouldReturnSuccess

  "fetchRequiredAction" should "retrieve a specified Required Action" in {
    authMgmt.fetchRequiredAction("test_action_provider").map(_.map { action =>
      storedAction.set(action)
    })
  }.shouldReturnSuccess

  "updateRequiredAction" should "update a specified Required Action" in {
    val oldAction = storedAction.get()
    val update = RequiredActionProvider.Update(
      alias     = oldAction.alias,
      config    = oldAction.config,
      name      = "test_action_2",
      priority  = 50
    )

    for {
      _       <- EitherT(authMgmt.updateRequiredAction("test_action_provider", update))
      action  <- EitherT(authMgmt.fetchRequiredAction("test_action_provider"))
    } yield action.name shouldBe "test_action_2"
  }.value.shouldReturnSuccess

  "lowerRequiredActionPriority" should "lower the priority of an existing Required Action" in {
    for {
      _       <- EitherT(authMgmt.lowerRequiredActionPriority("test_action_provider"))
      action  <- EitherT(authMgmt.fetchRequiredAction("test_action_provider"))
    } yield {
      action.priority > storedAction.get().priority
      storedAction.set(action)
    }
  }.value.shouldReturnSuccess

  "raiseRequiredActionPriority" should "raise the priority of an existing Required Action" in {
    for {
      _       <- EitherT(authMgmt.raiseRequiredActionPriority("test_action_provider"))
      action  <- EitherT(authMgmt.fetchRequiredAction("test_action_provider"))
    } yield action.priority < storedAction.get().priority
  }.value.shouldReturnSuccess

  "deleteRequiredAction" should "delete an existing Required Action" in {
    authMgmt.deleteRequiredAction("test_action_provider")
  }.shouldReturnSuccess

  "fetchAuthenticationFlows" should "retrieve a non-empty list of Authentication Flows" in {
    authMgmt.fetchAuthenticationFlows().map(_.map { flows =>
      flows shouldNot be (empty)
      val flow = flows.find(_.alias == "temp_flow")
      storedFlow.set(flow.get)
    })
  }.shouldReturnSuccess

  "fetchAuthenticationFlow" should "retrieve a specified Authentication Flow" in {
    authMgmt.fetchAuthenticationFlow(storedFlow.get().id)
  }.shouldReturnSuccess

  "deleteAuthenticationFlow" should "delete a specified Authentication Flow" in {
    authMgmt.deleteAuthenticationFlow(storedFlow.get().id)
  }.shouldReturnSuccess
}

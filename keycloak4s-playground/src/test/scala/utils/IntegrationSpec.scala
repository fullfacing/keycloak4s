package utils

import akka.util.ByteString
import com.fullfacing.akka.monix.task.backend.AkkaMonixHttpBackend
import com.fullfacing.keycloak4s.admin.monix.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.monix.services.{ClientScopes, _}
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakConfig}
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, Inspectors}
import sttp.client3.SttpBackend

import scala.concurrent.Future

class IntegrationSpec extends AsyncFlatSpec with Matchers with Inspectors {

  type T = ByteString

  /* Keycloak Server Configuration **/
  val authConfig: KeycloakConfig.Secret = KeycloakConfig.Secret("master", "admin-cli", "2864b21f-984a-4a71-b0e8-fc7162dc6f8b")//ServerInitializer.clientSecret)
  val keycloakConfig: ConfigWithAuth    = ConfigWithAuth("http", "127.0.0.1", 8080, "master", authConfig)

  /* Keycloak Client Implicits **/
  implicit val context: Scheduler = monix.execution.Scheduler.global
  implicit val backend: SttpBackend[Task, Observable[ByteString]] = AkkaMonixHttpBackend()
  implicit val client: KeycloakClient[T] = new KeycloakClient(keycloakConfig)(backend)

  /* Keycloak Services **/
  val attackDetService: AttackDetection[T]   = Keycloak.AttackDetection
  val authMgmt: AuthenticationManagement[T]  = Keycloak.AuthenticationManagement
  val clientScopeService: ClientScopes[T]    = Keycloak.ClientScopes
  val clientService: Clients[T]              = Keycloak.Clients
  val componentService: Components[T]        = Keycloak.Components
  val groupService: Groups[T]                = Keycloak.Groups
  val idProvService: IdentityProviders[T]    = Keycloak.IdentityProviders
  val protocolMapService: ProtocolMappers[T] = Keycloak.ProtocolMappers
  val realmService: RealmsAdmin[T]           = Keycloak.RealmsAdmin
  val rolesByIdService: RolesById[T]         = Keycloak.RolesById
  val roleService: Roles[T]                  = Keycloak.Roles
  val userService: Users[T]                  = Keycloak.Users

  /* Sub-Services **/
  val clientRoleService: roleService.ClientLevel.type = roleService.ClientLevel
  val realmRoleService: roleService.RealmLevel.type   = roleService.RealmLevel

  /* Implicit Helper Classes **/
  implicit class taskImpl[A, B](task: Task[Either[B, A]]) {
    def shouldReturnSuccess: Future[Assertion] = task.map { response =>
      response shouldBe a [scala.util.Right[_, _]]
    }.runToFuture
  }
}
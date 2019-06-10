package utils

import com.fullfacing.keycloak4s.admin.monix.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.monix.services._
import com.fullfacing.keycloak4s.core.models.KeycloakConfig
import com.fullfacing.transport.backends.MonixHttpBackendL
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest._

import scala.concurrent.Future

class IntegrationSpec extends AsyncFlatSpec with Matchers with Inspectors {

  /* Keycloak Server Configuration **/
  val authConfig      = KeycloakConfig.Auth("master", "admin-cli", ServerInitializer.clientSecret)
  val keycloakConfig  = KeycloakConfig("http", "127.0.0.1", 8080, "master", authConfig)

  /* Keycloak Client Implicits **/
  implicit val context: Scheduler         = monix.execution.Scheduler.global
  implicit val backend: MonixHttpBackendL = new MonixHttpBackendL(AsyncHttpClientMonixBackend())
  implicit val client: KeycloakClient     = new KeycloakClient(keycloakConfig)

  /* Keycloak Services **/
  val clientService: Clients            = Keycloak.Clients
  val groupService: Groups              = Keycloak.Groups
  val idProvService: IdentityProviders  = Keycloak.IdentityProviders
  val realmService: RealmsAdmin         = Keycloak.RealmsAdmin
  val rolesByIdService: RolesById       = Keycloak.RolesById
  val roleService: Roles                = Keycloak.Roles
  val scopeMapService: ScopeMappings    = Keycloak.ScopeMappings
  val userService: Users                = Keycloak.Users

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
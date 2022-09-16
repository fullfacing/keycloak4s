package utils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.fullfacing.keycloak4s.admin.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.services.{ClientScopes, _}
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakConfig}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, Inspectors}
import sttp.client3.SttpBackend
import sttp.client3.armeria.cats.ArmeriaCatsBackend

import scala.concurrent.Future

class IntegrationSpec extends AsyncFlatSpec with Matchers with Inspectors {

  /* Keycloak Server Configuration **/
  val authConfig: KeycloakConfig.Secret = KeycloakConfig.Secret("master", "admin-cli", ServerInitializer.clientSecret)
  val keycloakConfig: ConfigWithAuth    = ConfigWithAuth("http", "127.0.0.1", 8080, "master", authConfig, basePath = List.empty)

  /* Keycloak Client Implicits **/
    implicit val backend: SttpBackend[IO, Any] = ArmeriaCatsBackend[IO]()

    implicit val client: KeycloakClient[IO] = new KeycloakClient(keycloakConfig)

    /* Keycloak Services **/
    val attackDetService: AttackDetection[IO] = Keycloak.AttackDetection
    val authMgmt: AuthenticationManagement[IO] = Keycloak.AuthenticationManagement
    val clientScopeService: ClientScopes[IO] = Keycloak.ClientScopes
    val clientService: Clients[IO] = Keycloak.Clients
    val componentService: Components[IO] = Keycloak.Components
    val groupService: Groups[IO] = Keycloak.Groups
    val idProvService: IdentityProviders[IO] = Keycloak.IdentityProviders
    val protocolMapService: ProtocolMappers[IO] = Keycloak.ProtocolMappers
    val realmService: RealmsAdmin[IO] = Keycloak.RealmsAdmin
    val rolesByIdService: RolesById[IO] = Keycloak.RolesById
    val roleService: Roles[IO] = Keycloak.Roles
    val userService: Users[IO] = Keycloak.Users

    /* Sub-Services **/
    val clientRoleService: roleService.ClientLevel.type = roleService.ClientLevel
    val realmRoleService: roleService.RealmLevel.type = roleService.RealmLevel

  /* Implicit Helper Classes **/
  implicit class taskImpl[A, B](task: IO[Either[B, A]]) {
    def shouldReturnSuccess: Future[Assertion] = task.map { response =>
      response shouldBe a [scala.util.Right[_, _]]
    }.unsafeToFuture()
  }
}
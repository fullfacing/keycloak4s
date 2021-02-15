package suites.authz

import akka.util.ByteString
import com.fullfacing.akka.monix.bio.backend.AkkaMonixBioHttpBackend
import com.fullfacing.keycloak4s.admin.monix.bio.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.monix.bio.services.{Clients, RealmsAdmin}
import com.fullfacing.keycloak4s.authz.monix.bio.client.AuthzClient
import com.fullfacing.keycloak4s.authz.monix.bio.resources.{AuthorizationResource, PermissionResource, PolicyResource, ProtectedResource}
import com.fullfacing.keycloak4s.core.models._
import monix.bio.{IO, Task}
import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, Inspectors}
import sttp.client.{NothingT, SttpBackend}
import utils.ServerInitializer

import scala.concurrent.Future

trait IoIntegration extends AsyncFlatSpec with Matchers with Inspectors {
  implicit val context: Scheduler = monix.execution.Scheduler.global

  implicit class ioImpl[A, B](io: IO[B, A]) {
    def shouldReturnSuccess: Future[Assertion] = io.attempt.map { response =>
      response shouldBe a [scala.util.Right[_, _]]
    }.runToFuture
  }
}

object IoIntegration {
  type O = Observable[ByteString]

  val authConfig: KeycloakConfig.Secret = KeycloakConfig.Secret("master", "admin-cli", ServerInitializer.clientSecret)

  implicit val backend: SttpBackend[Task, Observable[ByteString], NothingT] = AkkaMonixBioHttpBackend()

  val realmService: RealmsAdmin[Observable[ByteString]] = {
    val config = ConfigWithAuth("http", "127.0.0.1", 8080, "master", authConfig)
    val client = new KeycloakClient[Observable[ByteString]](config)
    Keycloak.RealmsAdmin(client)
  }

  lazy val clientService: Clients[Observable[ByteString]] = {
    val config = ConfigWithAuth("http", "127.0.0.1", 8080, "AuthzRealm", authConfig)
    val client = new KeycloakClient[Observable[ByteString]](config)
    Keycloak.Clients(client)
  }

  def setup(): IO[KeycloakError, String] = {
    for {
      _  <- realmService.create(Realm.Create("AuthzRealm", "AuthzRealm", userManagedAccessAllowed = Some(true), enabled = Some(true)))
      c  <- clientService.fetch(clientId = Some("admin-cli"))
      id <- IO.fromOption(c.headOption.map(_.id), KeycloakThrowable(new Throwable("Client ID not found")))
      _  <- clientService.update(id, Client.Update(id, "admin-cli", serviceAccountsEnabled = Some(true), authorizationServicesEnabled = Some(true), publicClient = Some(false)))
      s  <- clientService.fetchClientSecret(id)
    } yield s.value.get
  }.memoizeOnSuccess

  val authzClient: IO[KeycloakError, AuthzClient[Observable[ByteString]]] = setup().flatMap { secret =>
    val authnConfig = KeycloakConfig.Secret("AuthzRealm", "admin-cli", secret)
    val authzConfig = ConfigWithAuth("http", "localhost", 8080, "AuthzRealm", authnConfig)
    AuthzClient.initialise(authzConfig)
  }.memoizeOnSuccess

  val resource: IO[KeycloakError, ProtectedResource[O]] = authzClient.map(new ProtectedResource[O]()(_)).memoizeOnSuccess
  val policy: IO[KeycloakError, PolicyResource[O]] = authzClient.map(new PolicyResource[O]()(_)).memoizeOnSuccess
  val permission: IO[KeycloakError, PermissionResource[O]] = authzClient.map(new PermissionResource[O]()(_)).memoizeOnSuccess
  val authorization: IO[KeycloakError, AuthorizationResource[O]] = authzClient.map(_.authorization()).memoizeOnSuccess
}

package suites

import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.admin.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.services.{Clients, Users}
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.enums.CredentialTypes
import org.scalatest.DoNotDiscover
import sttp.client3._
import utils.{Errors, IntegrationSpec}

import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class AttackDetectionTests extends IntegrationSpec {

  private val adKeycloakConfig  = ConfigWithAuth("http", "127.0.0.1", 8080, "AttackRealm", authConfig, basePath = Nil)

  override implicit val client: KeycloakClient[IO] = new KeycloakClient(adKeycloakConfig)

  override val clientService: Clients[IO] = Keycloak.Clients
  override val userService: Users[IO]     = Keycloak.Users

  val realm = "AttackRealm"
  val realmCreate = Realm.Create(
    realm = realm,
    id    = realm,
    enabled = Some(true),
    bruteForceProtected = Some(true),
    failureFactor = Some(3)
  )

  val clientCreate = Client.Create(clientId = "AttackClient")
  val userCredentials = Credential(`type` = Some(CredentialTypes.Password), value = Some("attackpassword"))
  val userCreate   = User.Create("attackuser", enabled = true, credentials = List(userCredentials))

  val tClient = new AtomicReference[Client]()
  val tUser   = new AtomicReference[User]()

  def login(realm: String = "AttackRealm", username: String = "attackuser", password: String): IO[Response[String]] = {
    val body = Map(
      "username"   -> username,
      "password"   -> password,
      "grant_type" -> "password",
      "client_id"  -> "AttackClient"
    )

    quickRequest
      .post(uri"http://localhost:8080/realms/$realm/protocol/openid-connect/token")
      .body(body)
      .send(backend)
  }

  val invalidLogin: IO[Response[String]] = login(password = "incorrect")

  "Test Setup" should "complete successfully" in {
    val IO =
      for {
        _  <- EitherT(realmService.create(realmCreate))
        _  <- EitherT(clientService.create(clientCreate))
        lc <- EitherT(clientService.fetch(clientId = Some(clientCreate.clientId)))
        c  <- EitherT.fromOption[IO](lc.find(_.clientId == clientCreate.clientId), Errors.CLIENT_NOT_FOUND)
        _  <- EitherT(userService.create(userCreate))
        lu <- EitherT(userService.fetch(username = Some(userCreate.username)))
        u  <- EitherT.fromOption[IO](lu.find(_.username == userCreate.username), Errors.USER_NOT_FOUND)
      } yield {
        tClient.set(c)
        tUser.set(u)
      }

    IO.value.shouldReturnSuccess
  }

  "fetchUserStatus" should "retrieve an object documenting all invalid login attempts by the given user" in {
    val IO =
      for {
        _ <- EitherT.right(login(password = userCredentials.value.getOrElse("")))
        b <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _ <- EitherT.right(invalidLogin)
        _ <- EitherT.right(invalidLogin)
        a <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures shouldBe 0
        a.numFailures shouldBe 2
      }

    IO.value.shouldReturnSuccess
  }

  "clearAllLoginFailures" should "reset all login failures by users on the realm" in {
    val IO =
      for {
        _  <- EitherT.right(invalidLogin)
        b  <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _  <- EitherT(attackDetService.clearAllLoginFailures(realm))
        a  <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        a.numFailures     shouldBe 0
      }

    IO.value.shouldReturnSuccess
  }

  it should "enable users that have been disabled due to too many login failures" in {
    val IO =
      for {
        _ <- EitherT.right(invalidLogin)
        _ <- EitherT.right(invalidLogin)
        _ <- EitherT.right(invalidLogin)
        b <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _ <- EitherT(attackDetService.clearAllLoginFailures(realm))
        a <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        b.disabled        shouldBe true
        a.numFailures     shouldBe 0
        a.disabled        shouldBe false
      }

    IO.value.shouldReturnSuccess
  }

  "clearUserLoginFailure" should "reset all login failures by the given user" in {
    val IO =
      for {
        _ <- EitherT.right(invalidLogin)
        b <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _ <- EitherT(attackDetService.clearUserLoginFailure(tUser.get.id, realm))
        a <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        a.numFailures     shouldBe 0
      }

    IO.value.shouldReturnSuccess
  }

  it should "enable the given user that has been disabled due to too many login failures" in {
    val IO =
      for {
        _ <- EitherT.right(invalidLogin)
        _ <- EitherT.right(invalidLogin)
        _ <- EitherT.right(invalidLogin)
        b <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _ <- EitherT(attackDetService.clearUserLoginFailure(tUser.get.id, realm))
        a <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        b.disabled        shouldBe true
        a.numFailures     shouldBe 0
        a.disabled        shouldBe false
      }

    IO.value.shouldReturnSuccess
  }

  "Test Reset" should "complete successfully" in
    realmService.delete(realm).shouldReturnSuccess

}

package suites

import java.util.concurrent.atomic.AtomicReference

import akka.util.ByteString
import cats.data.EitherT
import com.fullfacing.keycloak4s.admin.monix.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.monix.services.{Clients, Users}
import com.fullfacing.keycloak4s.core.models.enums.CredentialTypes
import com.fullfacing.keycloak4s.core.models._
import sttp.client3._
import monix.eval.Task
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class AttackDetectionTests extends IntegrationSpec {

  private val adKeycloakConfig  = ConfigWithAuth("http", "127.0.0.1", 8080, "AttackRealm", authConfig, basePath = Nil)
  private val adClient: KeycloakClient[T] = new KeycloakClient(adKeycloakConfig)

  override val clientService: Clients[T] = Keycloak.Clients[ByteString](adClient)
  override val userService: Users[T]     = Keycloak.Users[ByteString](adClient)

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

  def login(realm: String = "AttackRealm", username: String = "attackuser", password: String): Task[Response[String]] = {
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

  val invalidLogin: Task[Response[String]] = login(password = "incorrect")

  "Test Setup" should "complete successfully" in {
    val task =
      for {
        _  <- EitherT(realmService.create(realmCreate))
        _  <- EitherT(clientService.create(clientCreate))
        lc <- EitherT(clientService.fetch(clientId = Some(clientCreate.clientId)))
        c  <- EitherT.fromOption[Task](lc.find(_.clientId == clientCreate.clientId), Errors.CLIENT_NOT_FOUND)
        _  <- EitherT(userService.create(userCreate))
        lu <- EitherT(userService.fetch(username = Some(userCreate.username)))
        u  <- EitherT.fromOption[Task](lu.find(_.username == userCreate.username), Errors.USER_NOT_FOUND)
      } yield {
        tClient.set(c)
        tUser.set(u)
      }

    task.value.shouldReturnSuccess
  }

  "fetchUserStatus" should "retrieve an object documenting all invalid login attempts by the given user" in {
    val task =
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

    task.value.shouldReturnSuccess
  }

  "clearAllLoginFailures" should "reset all login failures by users on the realm" in {
    val task =
      for {
        _  <- EitherT.right(invalidLogin)
        b  <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _  <- EitherT(attackDetService.clearAllLoginFailures(realm))
        a  <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        a.numFailures     shouldBe 0
      }

    task.value.shouldReturnSuccess
  }

  it should "enable users that have been disabled due to too many login failures" in {
    val task =
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

    task.value.shouldReturnSuccess
  }

  "clearUserLoginFailure" should "reset all login failures by the given user" in {
    val task =
      for {
        _ <- EitherT.right(invalidLogin)
        b <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
        _ <- EitherT(attackDetService.clearUserLoginFailure(tUser.get.id, realm))
        a <- EitherT(attackDetService.fetchUserStatus(tUser.get.id, realm))
      } yield {
        b.numFailures > 0 shouldBe true
        a.numFailures     shouldBe 0
      }

    task.value.shouldReturnSuccess
  }

  it should "enable the given user that has been disabled due to too many login failures" in {
    val task =
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

    task.value.shouldReturnSuccess
  }

  "Test Reset" should "complete successfully" in
    realmService.delete(realm).shouldReturnSuccess

}

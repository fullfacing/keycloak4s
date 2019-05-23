package com.fullfacing.keycloak4s.admin

import cats.effect.{ContextShift, IO}
import com.fullfacing.keycloak4s.admin.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.services.{Clients, Groups, Roles, Users}
import com.fullfacing.keycloak4s.core.models.{KeycloakConfig, KeycloakError}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}

import scala.concurrent.ExecutionContext.global

class TestBase extends AsyncFlatSpec with Matchers {
  val clientSecret: String = ServerInitializer.clientSecret.unsafeRunSync()

  val authConfig = KeycloakConfig.Auth("master", "admin-cli", clientSecret)
  val keycloakConfig = KeycloakConfig("http", "127.0.0.1", 8080, "master", authConfig)

  implicit val context: ContextShift[IO] = IO.contextShift(global)
  implicit val backend: SttpBackend[IO, Nothing] = new CatsIoHttpBackendL(AsyncHttpClientCatsBackend[IO]())
  implicit val client: KeycloakClient[IO, Nothing] = new KeycloakClient[IO, Nothing](keycloakConfig)

  val roleService: Roles[IO, Nothing] = Keycloak.Roles[IO, Nothing]
  val realmRoleService: roleService.RealmLevel.type = roleService.RealmLevel
  val clientRoleService: roleService.ClientLevel.type = roleService.ClientLevel
  val clientService: Clients[IO, Nothing] = Keycloak.Clients[IO, Nothing]
  val groupService: Groups[IO, Nothing] = Keycloak.Groups[IO, Nothing]
  val userService: Users[IO, Nothing] = Keycloak.Users[IO, Nothing]

  def isSuccessful[A](response: Either[KeycloakError, A]): Assertion =
    response shouldBe a [scala.util.Right[_, _]]
}
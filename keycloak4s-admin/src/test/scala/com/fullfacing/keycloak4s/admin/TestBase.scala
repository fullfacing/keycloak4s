package com.fullfacing.keycloak4s.admin

import cats.effect.{ContextShift, IO}
import com.fullfacing.keycloak4s.admin.client.KeycloakClient
import com.fullfacing.keycloak4s.core.models.{KeycloakConfig, KeycloakError}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}

import scala.concurrent.ExecutionContext.global

class TestBase extends AsyncFlatSpec with Matchers {
  val authConfig = KeycloakConfig.Auth("master", "admin-cli", "ade6263d-2f16-405e-8c64-ea810ef536d0")
  val keycloakConfig = KeycloakConfig("http", "127.0.0.1", 8088, "test", authConfig)

  implicit val context: ContextShift[IO] = IO.contextShift(global)
  implicit val backend: SttpBackend[IO, Nothing] = new CatsIoHttpBackendL(AsyncHttpClientCatsBackend[IO]())
  implicit val client: KeycloakClient[IO, Nothing] = new KeycloakClient[IO, Nothing](keycloakConfig)

  def isSuccessful[A](response: Either[KeycloakError, A]): Assertion =
    response shouldBe a [scala.util.Right[_, _]]
}
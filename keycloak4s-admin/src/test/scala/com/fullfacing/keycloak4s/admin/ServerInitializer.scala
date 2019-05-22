package com.fullfacing.keycloak4s.admin

import java.util.UUID

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.TokenManager.TokenResponse
import com.fullfacing.keycloak4s.core.models.{Client, Credential, Role, User}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.softwaremill.sttp.json4s.asJson
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object ServerInitializer {

  implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  implicit val backend: SttpBackend[IO, Nothing] = AsyncHttpClientCatsBackend[IO]()

  private def getToken(): IO[Either[String, String]] = {
    val form = Map(
      "grant_type"  -> "password",
      "client_id"   -> "admin-cli",
      "username"    -> "admin",
      "password"    -> "admin"
    )

    sttp
      .post(uri"http://localhost:8080/auth/realms/master/protocol/openid-connect/token")
      .body(form)
      .response(asJson[TokenResponse])
      .mapResponse(_.access_token)
      .send()
      .map(_.body)
  }

  private def getClientId(token: String): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients?clientId=admin-cli")
      .header("Authorization", s"Bearer $token")
      .response(asJson[List[Client]])
      .mapResponse(clients => Either.fromOption(clients.headOption.map(_.id), "No Clients Found"))
      .send
      .map(_.body.flatten)
  }

  private def updateClient(token: String, clientId: UUID): IO[Either[String, String]] = {
    val client = Client.Update(
      id                      = clientId,
      clientId                = "admin-cli",
      publicClient            = Some(false),
      serviceAccountsEnabled  = Some(true)
    )

    sttp
      .put(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId")
      .header("Authorization", s"Bearer $token")
      .contentType("application/json")
      .body(write(client))
      .send()
      .map(_.body)
  }

  private def getServiceAccountUserId(token: String, clientId: UUID): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/service-account-user")
      .header("Authorization", s"Bearer $token")
      .response(asJson[User])
      .mapResponse(_.id)
      .send()
      .map(_.body)
  }

  private def getAdminRoleId(token: String): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/roles/admin")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Role])
      .mapResponse(_.id)
      .send()
      .map(_.body)
  }

  private def mapAdminRole(token: String, accountId: UUID, roleId: UUID): IO[Either[String, String]] = {
    val role = Role.Mapping(
      id    = Some(roleId),
      name  = Some("admin")
    )

    sttp
      .post(uri"http://localhost:8080/auth/admin/realms/master/users/$accountId/role-mappings/realm")
      .header("Authorization", s"Bearer $token")
      .contentType("application/json")
      .body(write(List(role)))
      .send()
      .map(_.body)
  }

  private def getClientSecret(token: String, clientId: UUID): IO[Either[String, String]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/client-secret")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Credential])
      .mapResponse(_.value)
      .send()
      .map(_.body)
  }

  private def initialize(): IO[String] = {
    for {
      token     <- EitherT(getToken())
      clientId  <- EitherT(getClientId(token))
      _         <- EitherT(updateClient(token, clientId))
      srvAccId  <- EitherT(getServiceAccountUserId(token, clientId))
      roleId    <- EitherT(getAdminRoleId(token))
      _         <- EitherT(mapAdminRole(token, srvAccId, roleId))
      secret    <- EitherT(getClientSecret(token, clientId))
    } yield secret
  }.fold(err => throw new Throwable(err), s => s)

  val clientSecret: IO[String] = initialize()
}

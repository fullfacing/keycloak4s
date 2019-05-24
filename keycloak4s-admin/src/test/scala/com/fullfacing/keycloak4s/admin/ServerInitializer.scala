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

  private implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  implicit val backend: SttpBackend[IO, Nothing] = new CatsIoHttpBackendL(AsyncHttpClientCatsBackend[IO]())

  /* Step 1: Retrieve an access token for the admin user. **/
  private def fetchToken(): IO[Either[String, String]] = {
    val form = Map(
      "grant_type"  -> "password",
      "client_id"   -> "admin-cli",
      "username"    -> "admin", // Modify if necessary.
      "password"    -> "admin"  // Modify if necessary.
    )

    sttp
      .post(uri"http://localhost:8080/auth/realms/master/protocol/openid-connect/token")
      .body(form)
      .response(asJson[TokenResponse])
      .mapResponse(_.access_token)
      .send()
      .map(_.body)
  }

  /* Step 2: Retrieve the ID for the admin-cli client. **/
  private def fetchClientId(token: String): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients?clientId=admin-cli")
      .header("Authorization", s"Bearer $token")
      .response(asJson[List[Client]])
      .mapResponse(clients => Either.fromOption(clients.headOption.map(_.id), "No Clients Found"))
      .send
      .map(_.body.flatten)
  }

  /* Step 3: Update admin-cli to disable public access and enable service accounts. **/
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

  /* Step 4: Retrieve the ID of the service account user. Can be performed asynchronously with step 5. **/
  private def fetchServiceAccountUserId(token: String, clientId: UUID): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/service-account-user")
      .header("Authorization", s"Bearer $token")
      .response(asJson[User])
      .mapResponse(_.id)
      .send()
      .map(_.body)
  }

  /* Step 5: Retrieve the ID of the admin role. Can be performed asynchronously with step 4. **/
  private def fetchAdminRoleId(token: String): IO[Either[String, UUID]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/roles/admin")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Role])
      .mapResponse(_.id)
      .send()
      .map(_.body)
  }

  /* Step 5: Map the admin role to the service account user. **/
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

  /* Step 6: Retrieve admin-cli's client secret. **/
  private def fetchClientSecret(token: String, clientId: UUID): IO[Either[String, String]] = {
    sttp
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/client-secret")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Credential])
      .mapResponse(_.value)
      .send()
      .map(_.body)
  }

  /**
   * Executes all actions required to setup a newly created Keycloak server instance.
   * Not intended for an already initialized server, in such a case failure is probable.
   *
   * The fetchToken() call expects a user to already have been created, with "admin" as its username and password.
   * If the username and/or password differs, fetchToken() will have to be modified accordingly.
   */
  private def initialize(): IO[String] = {
    for {
      token     <- EitherT(fetchToken())
      clientId  <- EitherT(fetchClientId(token))
      _         <- EitherT(updateClient(token, clientId))
      srvAccId  <- EitherT(fetchServiceAccountUserId(token, clientId))
      roleId    <- EitherT(fetchAdminRoleId(token))
      _         <- EitherT(mapAdminRole(token, srvAccId, roleId))
      secret    <- EitherT(fetchClientSecret(token, clientId))
    } yield secret
  }.fold(err => throw new Throwable(err), s => s)

  val clientSecret: IO[String] = initialize()
}

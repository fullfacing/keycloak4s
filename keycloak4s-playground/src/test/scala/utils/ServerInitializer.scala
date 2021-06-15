package utils

import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.TokenManager.TokenResponse
import com.fullfacing.keycloak4s.core.models.{Client, Credential, Role, User}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import sttp.client3._
import sttp.client3.json4s.asJson

import java.util.UUID

object ServerInitializer {

  /* The Serialization Object to be used by Sttp's Json4s API. **/
  private implicit val serializer: Serialization.type = org.json4s.jackson.Serialization

  /* Simplistic Synchronous Sttp Backend **/
  private implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  /* Step 1: Retrieve an access token for the admin user. **/
  private def fetchToken(): Either[String, String] = {
    val form = Map(
      "grant_type"  -> "password",
      "client_id"   -> "admin-cli",
      "username"    -> "admin", // Modify if necessary.
      "password"    -> "admin"  // Modify if necessary.
    )

    basicRequest
      .post(uri"http://localhost:8080/auth/realms/master/protocol/openid-connect/token")
      .body(form)
      .response(asJson[TokenResponse])
      .mapResponse(_.map(_.access_token).leftMap(_.getMessage))
      .send(backend)
      .map(_.body)
  }

  /* Step 2: Retrieve the ID of the admin-cli client. **/
  private def fetchClientId(token: String): Either[String, UUID] = {
    basicRequest
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients?clientId=admin-cli")
      .header("Authorization", s"Bearer $token")
      .response(???)//asJson[List[Client]])
//      .mapResponse(_.leftMap(_.getMessage).flatMap(_.headOption.map(_.id).toRight("No Clients Found")))
      .send(backend)
//      .map(_.body)
    ???
  }

  /* Step 3: Update admin-cli to disable public access and enable service accounts. **/
  private def updateClient(token: String, clientId: UUID): Either[String, String] = {
    val client = Client.Update(
      id                      = clientId,
      clientId                = "admin-cli",
      publicClient            = Some(false),
      serviceAccountsEnabled  = Some(true)
    )

    basicRequest
      .put(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId")
      .header("Authorization", s"Bearer $token")
      .contentType("application/json")
      .body(write(client))
      .send(backend)
      .map(_.body)
  }

  /* Step 4: Retrieve the ID of the service account user. (Steps 4 and 5 can be swapped or executed asynchronously) **/
  private def fetchServiceAccountUserId(token: String, clientId: UUID): Either[String, UUID] = {
    basicRequest
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/service-account-user")
      .header("Authorization", s"Bearer $token")
      .response(asJson[User])
      .mapResponse(_.map(_.id).leftMap(_.getMessage))
      .send(backend)
      .map(_.body)
  }

  /* Step 5: Retrieve the ID of the admin role. (Steps 4 and 5 can be swapped or executed asynchronously) **/
  private def fetchAdminRoleId(token: String): Either[String, UUID] = {
    basicRequest
      .get(uri"http://localhost:8080/auth/admin/realms/master/roles/admin")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Role])
      .mapResponse(_.map(_.id).leftMap(_.getMessage))
      .send(backend)
      .map(_.body)
  }

  /* Step 6: Map the admin role to the service account user. **/
  private def mapAdminRole(token: String, accountId: UUID, roleId: UUID): Either[String, String] = {
    val role = Role.Mapping(
      id    = roleId,
      name  = "admin"
    )

    basicRequest
      .post(uri"http://localhost:8080/auth/admin/realms/master/users/$accountId/role-mappings/realm")
      .header("Authorization", s"Bearer $token")
      .contentType("application/json")
      .body(write(List(role)))
      .send(backend)
      .map(_.body)
  }

  /* Step 7: Retrieve admin-cli's client secret. **/
  private def fetchClientSecret(token: String, clientId: UUID): Either[String, String] = {
    basicRequest
      .get(uri"http://localhost:8080/auth/admin/realms/master/clients/$clientId/client-secret")
      .header("Authorization", s"Bearer $token")
      .response(asJson[Credential])
      .mapResponse(_.leftMap(_.getMessage).flatMap(_.value.toRight("Client Secret Missing")))
      .send(backend)
      .map(_.body)
  }

  /**
   * Executes all actions required to setup a newly created Keycloak server instance.
   * Not intended for an already initialized server, in such a case it is probable that initialize() will fail.
   *
   * The fetchToken() call expects a user to already have been created, with "admin" as its username and password.
   * If the username and/or password differs the form in fetchToken() will have to be modified accordingly.
   */
  private def initialize(): String = {
    for {
      token     <- fetchToken()
      clientId  <- fetchClientId(token)
      _         <- updateClient(token, clientId)
      srvAccId  <- fetchServiceAccountUserId(token, clientId)
      roleId    <- fetchAdminRoleId(token)
      _         <- mapAdminRole(token, srvAccId, roleId)
      secret    <- fetchClientSecret(token, clientId)
    } yield secret
  }.fold(left => throw new Throwable(left), right => right)

  val clientSecret: String = initialize()
}

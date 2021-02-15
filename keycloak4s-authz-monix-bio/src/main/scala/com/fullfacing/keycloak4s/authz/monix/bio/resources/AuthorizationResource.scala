package com.fullfacing.keycloak4s.authz.monix.bio.resources

import cats.implicits._
import com.fullfacing.keycloak4s.admin.utils.Client.buildRequestInfo
import com.fullfacing.keycloak4s.admin.utils.Credentials
import com.fullfacing.keycloak4s.authz.monix.bio.Logging
import com.fullfacing.keycloak4s.authz.monix.bio.client.AuthzClient
import com.fullfacing.keycloak4s.authz.monix.bio.models.{AuthorizationRequest, ServerConfiguration}
import com.fullfacing.keycloak4s.authz.monix.bio.resources.AuthorizationResource._
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakError, KeycloakThrowable}
import monix.bio.{IO, UIO}
import org.json4s.Formats
import org.json4s.jackson.Serialization.read
import sttp.client.{NothingT, SttpBackend, UriContext, basicRequest}

import java.util.UUID

class AuthorizationResource[S](config: ConfigWithAuth, server: ServerConfiguration)
                              (implicit sttp: SttpBackend[IO[Throwable, *], S, NothingT]) {

  private implicit val formats: Formats = org.json4s.DefaultFormats

  private val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket"

  private def mapToRequestBody(request: AuthorizationRequest, responseMode: Option[String]): Seq[(String, String)] = {
    val permissions = request.permissions.map { permission =>
      val scopes = permission.scopes
      val value = if (scopes.nonEmpty) s"${permission.resourceId}#${scopes.mkString(",")}" else permission.resourceId
      "permission" -> value
    }

    val map = Map(
      "ticket"             -> request.ticket,
      "claim_token"        -> request.claimToken,
      "claim_token_format" -> request.claimTokenFormat,
      "pct"                -> request.pct,
      "rpt"                -> request.rptToken,
      "scope"              -> request.scope,
      "audience"           -> request.audience.orElse(Some(config.authn.clientId)),
      "subject_token"      -> request.subjectToken,
      "response_include_resource_name" -> request.includeResourceName.map(_.toString),
      "response_permissions_limit"     -> request.limit.map(_.toString),
      "response_mode"                  -> responseMode
    )
      .collect { case (key, Some(value)) => key -> value } ++
      Credentials.access(request.auth.getOrElse(config.authn)) +
      ("grant_type" -> GRANT_TYPE)

      map.toList ++ permissions
  }

  private def request[A <: AnyRef : Manifest](authRequest: AuthorizationRequest, response: Option[String]): IO[KeycloakError, A] = {
    val body = mapToRequestBody(authRequest, response)
    val uri  = uri"${server.token_endpoint}"
    val cId  = UUID.randomUUID()
    val info = buildRequestInfo(uri, "POST", body)

    val request = basicRequest
      .post(uri)
      .body(body: _*)
      .send()
      .mapError(KeycloakThrowable.apply)
      .flatMap(AuthzClient.liftM(_, info))
      .map { response =>
        Logging.requestSuccessful(response, cId)
        read[A](response)
      }

    UIO(Logging.requestSent(info, cId))
      .flatMap(_ => request)
      .tapError(e => UIO(Logging.requestFailed(cId, e)))
  }

  def authorizeWithPermissionResponse(authRequest: AuthorizationRequest): IO[KeycloakError, List[PermissionResponse]] = {
    request[List[PermissionResponse]](authRequest, Some("permissions"))
  }

  def authorizeWithDecisionResponse(authRequest: AuthorizationRequest): IO[KeycloakError, DecisionResponse] = {
    request[DecisionResponse](authRequest, Some("decision"))
  }

  def authorize(authRequest: AuthorizationRequest): IO[KeycloakError, TokenResponse] = {
    request[TokenResponse](authRequest, None)
  }
}

object AuthorizationResource {

  final case class TokenResponse(access_token: String,
                                 expires_in: Long,
                                 refresh_expires_in: Long,
                                 refresh_token: Option[String] = None,
                                 token_type: String,
                                 id_token: Option[String] = None,
                                 `not-before-policy`: Int,
                                 session_state: Option[String] = None,
                                 scope: Option[String] = None)

  final case class DecisionResponse(result: Boolean)

  final case class PermissionResponse(rsid: String,
                                      rsname: Option[String] = None,
                                      scopes: List[String] = List.empty[String])

}
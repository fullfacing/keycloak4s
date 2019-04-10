package com.fullfacing.keycloak4s.client

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.client.serialization.JsonFormats.default
import com.fullfacing.keycloak4s.client.TokenManager.{Token, TokenResponse}
import com.fullfacing.keycloak4s.models.{KeycloakAdminException, RequestInfo}
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp.{SttpBackend, _}
import org.json4s.jackson.Serialization

abstract class TokenManager[F[_] : Concurrent, -S](config: KeycloakConfig)(implicit client: SttpBackend[F, S]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  protected val F: MonadError[F] = client.responseMonad

  protected def buildRequestInfo(path: Seq[String], protocol: String, body: Any): RequestInfo = {
    RequestInfo(
      path      = path.mkString("/"),
      protocol  = protocol,
      body      = body match {
        case _: Unit  => None
        case a        => Some(a)
      }
    )
  }

  protected def buildError(response: Response[_], requestInfo: RequestInfo): KeycloakAdminException = {
    KeycloakAdminException(
      code        = response.code,
      headers     = response.headers,
      body        = response.rawErrorBody.fold(new String(_, StandardCharsets.UTF_8), _ => "N/A"),
      statusText  = response.statusText,
      requestInfo = requestInfo
    )
  }

  protected def liftM[A](response: Response[A], requestInfo: RequestInfo): Either[KeycloakAdminException, A] = {
    response.body.leftMap(_ => buildError(response, requestInfo))
  }

  private val tokenEndpoint =
    uri"http://${config.host}:${config.port}/auth/realms/${config.authn.realm}/protocol/openid-connect/token"

  private val password = Map(
    "grant_type" -> "client_credentials",
    "client_id" -> config.authn.clientId,
    "client_secret" -> config.authn.clientSecret
  )


  val ref: AtomicReference[Token] = new AtomicReference()

  private def refresh(token: Token): Map[String, String] = Map(
    "client_id" -> config.authn.clientId,
    "client_secret" -> config.authn.clientSecret,
    "refresh_token" -> token.refresh,
    "grant_type" -> "refresh_token"
  )

  /**
    * Authenticate the application with Keycloak, returning an access and refresh token.
    *
    * @return
    */
  private def issueAccessToken(): F[Either[KeycloakAdminException, Token]] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)

    val a = sttp.post(tokenEndpoint)
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    Concurrent[F].map(a)(liftM(_, requestInfo))
  }

  private def refreshAccessToken(t: Token): F[Either[KeycloakAdminException, Token]] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)

    val a = sttp.post(tokenEndpoint)
      .body(refresh(t))
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    Concurrent[F].map(a)(liftM(_, requestInfo))
  }



  /**
    * Extract all the relevant data from the Keycloak Token Response.
    *
    * @param response the oidc token response.
    * @return a new token instance.
    */
  private def mapToToken(response: TokenResponse): Token = {
    val instant = Instant.now()
    Token(
      access = response.access_token,
      refresh = response.refresh_token,
      refreshAt = instant.plusSeconds(response.expires_in),
      authenticateAt = instant.plusSeconds(response.refresh_expires_in)
    )
  }


  /**
    * Inspect the status of the token, reissuing a new access token using the password
    * credentials grant type, or refreshing the existing token using the refresh_token grant type.
    *
    * If the access token is still valid it simply returns the token unchanged.
    * @return
    */
  private def validateToken(): F[Either[KeycloakAdminException, Token]] = {

    def setToken(a: Either[KeycloakAdminException, Token]): Either[KeycloakAdminException, Token] = {
      a.map { nToken =>
        ref.set(nToken)
        nToken
      }
    }

    val token = ref.get()
    if (token == null) {
      Concurrent[F].map(issueAccessToken())(setToken)
    } else {
      val epoch = Instant.now()
      if (epoch.isAfter(token.authenticateAt)) {
        Concurrent[F].map(issueAccessToken())(setToken)
      } else if (epoch.isAfter(token.refreshAt)) {
        Concurrent[F].map(refreshAccessToken(token))(setToken)
      } else {
        Concurrent[F].pure(token.asRight)
      }
    }
  }

  def withAuth[A](request: RequestT[Id, A, Nothing]): F[Either[KeycloakAdminException, RequestT[Id, A, Nothing]]] = {
    Concurrent[F].map(validateToken())(_.map(tkn =>request.auth.bearer(tkn.access)))
  }
}

object TokenManager {

  final case class TokenResponse(access_token: String,
                                 expires_in: Long,
                                 refresh_expires_in: Long,
                                 refresh_token: String,
                                 token_type: String,
                                 `not-before-policy`: Int,
                                 session_state: String,
                                 scope: String)

  final case class Token(access: String,
                         refresh: String,
                         refreshAt: Instant,
                         authenticateAt: Instant)

}
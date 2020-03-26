package com.fullfacing.keycloak4s.admin.client

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.TokenManager.{Token, TokenResponse}
import com.fullfacing.keycloak4s.admin.handles.Logging
import com.fullfacing.keycloak4s.admin.handles.Logging.handleLogging
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakConfig, KeycloakSttpException, RequestInfo}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.monad.MonadError
import sttp.client.{Identity, NoBody, NothingT, RequestT, Response, SttpBackend, _}

abstract class TokenManager[F[_] : Concurrent, -S](config: ConfigWithAuth)(implicit client: SttpBackend[F, S, NothingT]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  protected val F: MonadError[F] = client.responseMonad

  type SttpResponse[A] = Either[ResponseError[Exception], A]

  protected def buildRequestInfo(path: Seq[String], protocol: String, body: Any): RequestInfo = {
    RequestInfo(
      path      = path.mkString("/"),
      protocol  = protocol,
      body      = body match {
        case _: Unit  => None
        case NoBody   => None
        case a        => Some(a)
      }
    )
  }

  protected def buildError(response: Response[_], leftBody: String, requestInfo: RequestInfo): KeycloakSttpException = {
    KeycloakSttpException(
      code        = response.code.code,
      headers     = response.headers.map(h => h.name -> h.value),
      body        = leftBody,
      statusText  = response.statusText,
      requestInfo = requestInfo
    )
  }

  protected def liftM[A](response: Response[Either[String, A]], requestInfo: RequestInfo): Either[KeycloakSttpException, A] = {
    response.body.leftMap(l => buildError(response, l, requestInfo))
  }

  private val tokenEndpoint =
    uri"${config.scheme}://${config.host}:${config.port}/auth/realms/${config.authn.realm}/protocol/openid-connect/token"

  private val password = config.authn match {
    case KeycloakConfig.Password(_, clientId, username, pass) =>
      Map(
        "grant_type"    -> "password",
        "client_id"     -> clientId,
        "username"      -> username,
        "password"      -> pass
      )
    case KeycloakConfig.Secret(_, clientId, clientSecret) =>
      Map(
        "grant_type"    -> "client_credentials",
        "client_id"     -> clientId,
        "client_secret" -> clientSecret
      )
  }

  val ref: AtomicReference[Token] = new AtomicReference()

  private def refresh(token: Token): Map[String, String] = config.authn match {
    case KeycloakConfig.Secret(_, _, clientSecret) =>
      Map(
        "client_id"     -> config.authn.clientId,
        "refresh_token" -> token.refresh,
        "client_secret" -> clientSecret,
        "grant_type"    -> "refresh_token"
      )
    case _: KeycloakConfig.Password =>
      Map(
        "client_id"     -> config.authn.clientId,
        "refresh_token" -> token.refresh,
        "grant_type"    -> "refresh_token"
      )
  }

  /**
    * Authenticate the application with Keycloak, returning an access and refresh token.
    *
    * @return
    */
  def issueAccessToken()(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)

    val sendF = Concurrent[F].unit.flatMap { _ =>
      Logging.tokenRequest(config.realm, cId)

      basicRequest.post(tokenEndpoint)
        .body(password)
        .response(asJson[TokenResponse])
        .mapResponse(mapToToken)
        .send()
    }

    sendF.map(liftM(_, requestInfo)).map {
      handleLogging(_)(
        success = _ => Logging.tokenReceived(config.realm, cId),
        failure = Logging.tokenRequestFailed(config.realm, cId, _)
      )
    }
  }

  private def refreshAccessToken(t: Token)(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    val body = refresh(t)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)

    val sendF = Concurrent[F].unit.flatMap { _ =>
      Logging.tokenRefresh(config.realm, cId)

      basicRequest.post(tokenEndpoint)
        .body(body)
        .response(asJson[TokenResponse])
        .mapResponse(mapToToken)
        .send()
    }

    sendF.map(liftM(_, requestInfo)).map {
      handleLogging(_)(
        success = _ => Logging.tokenRefreshed(config.realm, cId),
        failure = Logging.tokenRefreshFailed(config.realm, cId, _)
      )
    }
  }

  /**
    * Extract all the relevant data from the Keycloak Token Response.
    *
    * @param response the oidc token response.
    * @return a new token instance.
    */
  private def mapToToken(response: SttpResponse[TokenResponse]): Either[String, Token] = response.map { res =>
    val instant = Instant.now()
    Token(
      access          = res.access_token,
      refresh         = res.refresh_token,
      refreshAt       = instant.plusSeconds(res.expires_in),
      authenticateAt  = instant.plusSeconds(res.refresh_expires_in)
    )
  }.leftMap(_.body)

  /**
    * Inspect the status of the token, reissuing a new access token using the password
    * credentials grant type, or refreshing the existing token using the refresh_token grant type.
    *
    * If the access token is still valid it simply returns the token unchanged.
    * @return
    */
  private def validateToken()(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {

    def setToken(a: Either[KeycloakSttpException, Token]): Either[KeycloakSttpException, Token] = {
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

  def withAuth[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID): F[Either[KeycloakSttpException, RequestT[Identity, A, Nothing]]] = {
    Concurrent[F].map(validateToken())(_.map(tkn => request.auth.bearer(tkn.access)))
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

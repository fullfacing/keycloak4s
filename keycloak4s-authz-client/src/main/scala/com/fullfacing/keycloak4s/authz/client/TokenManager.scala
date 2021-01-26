package com.fullfacing.keycloak4s.authz.client

import cats.implicits._
import com.fullfacing.keycloak4s.admin.Logging
import TokenManager._
import com.fullfacing.keycloak4s.authz.client.models.ServerConfiguration
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.{Identity, NothingT, RequestT, SttpBackend, _}
import sttp.model.Uri

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

abstract class TokenManager[-S](config: ConfigWithAuth, server: ServerConfiguration)(implicit client: SttpBackend[IO[Throwable, *], S, NothingT]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

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
    case KeycloakConfig.PasswordWithSecret(_, clientId, username, pass, secret) =>
      Map(
        "grant_type"    -> "password",
        "client_id"     -> clientId,
        "username"      -> username,
        "password"      -> pass,
        "client_secret" -> secret
      )
  }

  val ref: AtomicReference[Token] = new AtomicReference()

  private def refresh(token: TokenWithRefresh): Map[String, String] = config.authn match {
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
    case KeycloakConfig.PasswordWithSecret(_, _, _, _, clientSecret) =>
      Map(
        "client_id"     -> config.authn.clientId,
        "refresh_token" -> token.refresh,
        "client_secret" -> clientSecret,
        "grant_type"    -> "refresh_token"
      )
  }

  def handleLogging[A](result: IO[Throwable, A])(implicit cId: UUID): IO[Throwable, A] = {
    result.map({ res => Logging.tokenReceived(config.realm, cId); res }).mapError { ex =>
      Logging.tokenRequestFailed(config.realm, cId, ex); ex
    }
  }

  /**
   * Authenticate the application with Keycloak, returning an access and refresh token.
   *
   * @return
   */
  def issueAccessToken()(implicit cId: UUID): IO[KeycloakError, Token] = {
    val requestInfo = AuthzClient.buildRequestInfo(server.tokenEndpoint, "POST", password)
    val request = basicRequest
      .post(Uri.apply(server.tokenEndpoint))
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRequest(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- AuthzClient.liftM(response, requestInfo)
    } yield token
  }

  def refreshAccessToken(t: TokenWithRefresh)(implicit cId: UUID): IO[KeycloakError, Token] = {
    val body = refresh(t)
    val requestInfo = AuthzClient.buildRequestInfo(server.tokenEndpoint, "POST", body)
    val request = basicRequest
      .post(Uri.apply(server.tokenEndpoint))
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRefresh(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- AuthzClient.liftM(response, requestInfo)
    } yield token
  }

  private def issueAndSetAccessToken()(implicit cId: UUID): IO[KeycloakError, Token] = {
    for {
      t <- issueAccessToken()
      _ <- setToken(t)
    } yield t
  }

  private def refreshAndSetAccessToken(t: TokenWithRefresh)(implicit cId: UUID): IO[KeycloakError, Token] = {
    for {
      t0 <- refreshAccessToken(t)
      _  <- setToken(t0)
    } yield t
  }

  /**
   * Extract all the relevant data from the Keycloak Token Response.
   *
   * @param response the oidc token response.
   * @return a new token instance.
   */
  private def mapToToken(response: Either[ResponseError[Exception], TokenResponse]): Either[String, Token] = {
    response.map { res =>
      val instant = Instant.now()
      res.refresh_token.fold[Token] {
        TokenWithoutRefresh(
          access         = res.access_token,
          authenticateAt = instant.plusSeconds(res.expires_in)
        )
      } { refresh =>
        TokenWithRefresh(
          access          = res.access_token,
          refresh         = refresh,
          refreshAt       = instant.plusSeconds(res.expires_in),
          authenticateAt  = instant.plusSeconds(res.refresh_expires_in)
        )
      }
    }.leftMap(_.getMessage)
  }

  private def getToken: UIO[Option[Token]] = UIO.delay(Option(ref.get))
  private def setToken(token: Token): UIO[Unit] = UIO(ref.set(token))

  /**
   * Inspect the status of the token, reissuing a new access token using the password
   * credentials grant type, or refreshing the existing token using the refresh_token grant type.
   *
   * If the access token is still valid it simply returns the token unchanged.
   * @return
   */
  private def evaluateToken()(implicit cId: UUID): IO[KeycloakError, Token] = getToken.flatMap { token =>
    lazy val epoch = Instant.now()
    token.fold {
      issueAndSetAccessToken()
    } {
      case t if epoch.isAfter(t.authenticateAt) =>
        issueAndSetAccessToken()

      case t @ TokenWithRefresh(_, _, refreshAt, _) if epoch.isAfter(refreshAt) =>
        refreshAndSetAccessToken(t)
          .orElse(issueAndSetAccessToken())

      case t =>
        UIO(t)
    }
  }

  protected def withAuthNewToken[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    issueAndSetAccessToken().map(tkn => request.auth.bearer(tkn.access))
  }

  def withAuth[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    evaluateToken().map(tkn => request.auth.bearer(tkn.access))
  }
}

object TokenManager {

  final case class TokenResponse(access_token: String,
                                 expires_in: Long,
                                 refresh_expires_in: Long,
                                 refresh_token: Option[String] = None,
                                 token_type: String,
                                 `not-before-policy`: Int,
                                 session_state: Option[String] = None,
                                 scope: String)

  sealed trait Token {
    val access: String
    val authenticateAt: Instant
  }

  final case class TokenWithoutRefresh(access: String,
                                       authenticateAt: Instant) extends Token

  final case class TokenWithRefresh(access: String,
                                    refresh: String,
                                    refreshAt: Instant,
                                    authenticateAt: Instant) extends Token
}

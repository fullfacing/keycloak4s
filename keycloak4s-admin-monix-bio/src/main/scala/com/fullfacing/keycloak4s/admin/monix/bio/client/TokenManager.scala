package com.fullfacing.keycloak4s.admin.monix.bio.client

import cats.implicits._
import com.fullfacing.keycloak4s.admin.monix.bio.client.TokenManager._
import com.fullfacing.keycloak4s.admin.monix.bio.handles.Logging
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization
import sttp.client3.json4s._
import sttp.client3.{Identity, NoBody, RequestT, Response, SttpBackend, _}

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

abstract class TokenManager(config: ConfigWithAuth)(implicit client: SttpBackend[IO[Throwable, *], Any]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

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

  private val tokenEndpoint =
    uri"${config.buildBaseUri}/realms/${config.authn.realm}/protocol/openid-connect/token"

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
  }

  def handleLogging[A](result: IO[Throwable, A])(implicit cId: UUID): IO[Throwable, A] = {
    result.map({ res => Logging.tokenReceived(config.realm, cId); res }).mapError { ex =>
      Logging.tokenRequestFailed(config.realm, cId, ex); ex
    }
  }

  protected def liftM[A](response: Response[Either[String, A]], requestInfo: RequestInfo): IO[KeycloakError, A] = {
    IO.fromEither(response.body).mapError(l => buildError(response, l, requestInfo))
  }

  /**
   * Authenticate the application with Keycloak, returning an access and refresh token.
   *
   * @return
   */
  def issueAccessToken()(implicit cId: UUID): IO[KeycloakError, Token] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)
    val request = basicRequest
      .post(tokenEndpoint)
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRequest(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- liftM(response, requestInfo)
    } yield token
  }

  private def refreshAccessToken(t: TokenWithRefresh)(implicit cId: UUID): IO[KeycloakError, Token] = {
    val body = refresh(t)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)
    val request = basicRequest
      .post(tokenEndpoint)
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRefresh(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- liftM(response, requestInfo)
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
  private def mapToToken(response: Either[ResponseException[String, Exception], TokenResponse]): Either[String, Token] = {
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

  protected def withAuthNewToken[A](request: RequestT[Identity, A, Any])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Any]] = {
    issueAndSetAccessToken().map(tkn => request.auth.bearer(tkn.access))
  }

  def withAuth[A](request: RequestT[Identity, A, Any])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Any]] = {
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

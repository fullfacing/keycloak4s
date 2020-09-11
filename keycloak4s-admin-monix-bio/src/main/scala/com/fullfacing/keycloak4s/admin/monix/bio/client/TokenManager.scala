package com.fullfacing.keycloak4s.admin.monix.bio.client

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.TokenManager.{Token, TokenResponse}
import com.fullfacing.keycloak4s.admin.handles.Logging
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.{Identity, NoBody, NothingT, RequestT, Response, SttpBackend, _}

abstract class TokenManager[-S](config: ConfigWithAuth)(implicit client: SttpBackend[IO[Throwable, *], S, NothingT]) {

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

  private def refreshAccessToken(t: Token)(implicit cId: UUID): IO[KeycloakError, Token] = {
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

  private def refreshAndSetAccessToken(t: Token)(implicit cId: UUID): IO[KeycloakError, Token] = {
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
      Token(
        access          = res.access_token,
        refresh         = res.refresh_token,
        refreshAt       = instant.plusSeconds(res.expires_in),
        authenticateAt  = instant.plusSeconds(res.refresh_expires_in)
      )
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
  private def validateToken()(implicit cId: UUID): IO[KeycloakError, Token] = {
    for {
      current <- getToken
      epoch    = Instant.now()
      res     <- current match {
        case None => issueAndSetAccessToken()
        case Some(token) if epoch.isAfter(token.authenticateAt) => issueAndSetAccessToken()
        case Some(token) if epoch.isAfter(token.refreshAt) => refreshAndSetAccessToken(token) orElse issueAndSetAccessToken()
        case Some(token) => UIO.now(token)
      }
    } yield res
  }

  protected def withAuthNewToken[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    issueAndSetAccessToken().map(tkn => request.auth.bearer(tkn.access))
  }

  def withAuth[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    validateToken().map(tkn => request.auth.bearer(tkn.access))
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
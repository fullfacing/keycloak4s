package com.fullfacing.keycloak4s.admin.monix.bio.client

import cats.implicits._
import com.fullfacing.keycloak4s.admin.models._
import com.fullfacing.keycloak4s.admin.utils.Client._
import com.fullfacing.keycloak4s.admin.utils.Logging
import com.fullfacing.keycloak4s.admin.utils.TokenManager._
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.{Identity, NothingT, RequestT, Response, SttpBackend, _}

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

abstract class TokenManager[-S](config: ConfigWithAuth)(implicit client: SttpBackend[IO[Throwable, *], S, NothingT]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  private val tokenEndpoint =
    uri"${config.buildBaseUri}/realms/${config.authn.realm}/protocol/openid-connect/token"

  val ref: AtomicReference[Token] = new AtomicReference()

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
    val body = password(config)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)
    val request = basicRequest
      .post(tokenEndpoint)
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(TokenResponse.mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRequest(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- liftM(response, requestInfo)
    } yield token
  }

  private def refreshAccessToken(t: TokenWithRefresh)(implicit cId: UUID): IO[KeycloakError, Token] = {
    val body = refresh(t, config)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)
    val request = basicRequest
      .post(tokenEndpoint)
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(TokenResponse.mapToToken)
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
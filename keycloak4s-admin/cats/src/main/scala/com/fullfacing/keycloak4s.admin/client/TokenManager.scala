package com.fullfacing.keycloak4s.admin.client

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.admin.models._
import com.fullfacing.keycloak4s.admin.utils.Client._
import com.fullfacing.keycloak4s.admin.utils.Logging
import com.fullfacing.keycloak4s.admin.utils.Logging.handleLogging
import com.fullfacing.keycloak4s.admin.utils.Credentials._
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakSttpException, RequestInfo}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.monad.MonadError
import sttp.client.{Identity, NothingT, RequestT, Response, SttpBackend, _}

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

abstract class TokenManager[F[_] : Concurrent, -S](config: ConfigWithAuth)(implicit client: SttpBackend[F, S, NothingT]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  protected val F: MonadError[F] = client.responseMonad

  protected def liftM[A](response: Response[Either[String, A]], requestInfo: RequestInfo): Either[KeycloakSttpException, A] = {
    response.body.leftMap(l => buildError(response, l, requestInfo))
  }

  private val tokenEndpoint =
    uri"${config.buildBaseUri}/realms/${config.authn.realm}/protocol/openid-connect/token"

  val ref: AtomicReference[Token] = new AtomicReference()

  /**
    * Authenticate the application with Keycloak, returning an access and refresh token.
    *
    * @return
    */
  def issueAccessToken()(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    val body = access(config.authn)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)

    val sendF = Concurrent[F].unit.flatMap { _ =>
      Logging.tokenRequest(config.realm, cId)

      basicRequest.post(tokenEndpoint)
        .body(body)
        .response(asJson[TokenResponse])
        .mapResponse(TokenResponse.mapToToken)
        .send()
    }

    sendF.map(liftM(_, requestInfo)).map {
      handleLogging(_)(
        success = _ => Logging.tokenReceived(config.realm, cId),
        failure = Logging.tokenRequestFailed(config.realm, cId, _)
      )
    }
  }

  private def refreshAccessToken(t: TokenWithRefresh)(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    val body = refresh(t, config.authn)
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", body)

    val sendF = Concurrent[F].unit.flatMap { _ =>
      Logging.tokenRefresh(config.realm, cId)

      basicRequest.post(tokenEndpoint)
        .body(body)
        .response(asJson[TokenResponse])
        .mapResponse(TokenResponse.mapToToken)
        .send()
    }

    sendF.map(liftM(_, requestInfo)).map {
      handleLogging(_)(
        success = _ => Logging.tokenRefreshed(config.realm, cId),
        failure = Logging.tokenRefreshFailed(config.realm, cId, _)
      )
    }
  }

  private def issueAndSetAccessToken()(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    Concurrent[F].flatTap(issueAccessToken()) {
      case Right(value) => setToken(value)
      case _            => Concurrent[F].unit
    }
  }

  private def refreshAndSetAccessToken(t: TokenWithRefresh)(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = {
    Concurrent[F].flatTap(refreshAccessToken(t)) {
      case Right(value) => setToken(value)
      case _            => Concurrent[F].unit
    }
  }

  private def getToken: F[Option[Token]] = Concurrent[F].delay(Option(ref.get))
  private def setToken(token: Token): F[Unit] = Concurrent[F].delay(ref.set(token))

  /**
    * Inspect the status of the token, reissuing a new access token using the password
    * credentials grant type, or refreshing the existing token using the refresh_token grant type.
    *
    * If the access token is still valid it simply returns the token unchanged.
    * @return
    */
  private def evaluateToken()(implicit cId: UUID): F[Either[KeycloakSttpException, Token]] = getToken.flatMap { token =>
    lazy val epoch = Instant.now()
    token.fold {
      issueAndSetAccessToken()
    } {
      case t if epoch.isAfter(t.authenticateAt) =>
        issueAndSetAccessToken()

      case t @ TokenWithRefresh(_, _, refreshAt, _) if epoch.isAfter(refreshAt) =>
        EitherT(refreshAndSetAccessToken(t))
          .orElse(EitherT(issueAndSetAccessToken()))
          .value

      case t =>
        Concurrent[F].pure(t.asRight)
    }
  }

  protected def withAuthNewToken[A](request: RequestT[Identity, A, Nothing])
                                   (implicit cId: UUID): F[Either[KeycloakSttpException, RequestT[Identity, A, Nothing]]] = {
    Concurrent[F].map(issueAndSetAccessToken())(_.map(tkn => request.auth.bearer(tkn.access)))
  }

  def withAuth[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID): F[Either[KeycloakSttpException, RequestT[Identity, A, Nothing]]] = {
    Concurrent[F].map(evaluateToken())(_.map(tkn => request.auth.bearer(tkn.access)))
  }
}
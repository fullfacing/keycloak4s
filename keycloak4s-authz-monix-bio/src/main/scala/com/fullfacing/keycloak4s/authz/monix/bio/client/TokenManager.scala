package com.fullfacing.keycloak4s.authz.monix.bio.client

import cats.implicits._
import com.fullfacing.keycloak4s.admin.models.{Token, TokenResponse, TokenWithRefresh}
import com.fullfacing.keycloak4s.admin.utils.Client._
import com.fullfacing.keycloak4s.admin.utils.Credentials._
import com.fullfacing.keycloak4s.admin.utils.{Credentials, Logging}
import com.fullfacing.keycloak4s.authz.monix.bio.models.{AuthorizationRequest, IntrospectionResponse, ServerConfiguration}
import com.fullfacing.keycloak4s.authz.monix.bio.serialization.IntrospectionSerializer
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats
import monix.bio.{IO, UIO}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import sttp.client.json4s._
import sttp.client.{Identity, NothingT, RequestT, SttpBackend, _}

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

abstract class TokenManager[-S](config: ConfigWithAuth, server: ServerConfiguration)(implicit client: SttpBackend[IO[Throwable, *], S, NothingT]) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  private implicit val formats: Formats = JsonFormats.default + new IntrospectionSerializer()

  private val ref: AtomicReference[Token] = new AtomicReference()

  private val password = Credentials.password(config)

  def handleLogging[A](result: IO[Throwable, A])(implicit cId: UUID): IO[Throwable, A] = {
    result
      .flatTap(_ => IO.now(Logging.tokenReceived(config.realm, cId)))
      .tapError(ex => IO.now(Logging.tokenRequestFailed(config.realm, cId, ex)))
  }

  /** Authenticate the application with Keycloak, returning an access and refresh token. */
  def issueAccessToken()(implicit cId: UUID): IO[KeycloakError, Token] = {
    val requestInfo = buildRequestInfo(server.token_endpoint, "POST", password)
    val request = basicRequest
      .post(uri"${server.token_endpoint}")
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(TokenResponse.mapToToken)
      .send()

    for {
      _        <- UIO(Logging.tokenRequest(config.realm, cId))
      response <- handleLogging(request).mapError(KeycloakThrowable.apply)
      token    <- AuthzClient.liftM(response, requestInfo)
    } yield token
  }

  def refreshAccessToken(t: TokenWithRefresh)(implicit cId: UUID): IO[KeycloakError, Token] = {
    val body = refresh(t, config)
    val requestInfo = buildRequestInfo(server.token_endpoint, "POST", body)
    val request = basicRequest
      .post(uri"${server.token_endpoint}")
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(TokenResponse.mapToToken)
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

  private def getToken: UIO[Option[Token]]      = UIO.delay(Option(ref.get))
  private def setToken(token: Token): UIO[Unit] = UIO(ref.set(token))

  /**
   * Inspect the status of the token, reissuing a new access token using the password
   * credentials grant type, or refreshing the existing token using the refresh_token grant type.
   *
   * If the access token is still valid it is simply returned.
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

  private def introspectionBody(token: String): Map[String, String] = Map(
    "token_type_hint" -> "requesting_party_token",
    "token"           -> token
  )

  def introspectToken(token: String): IO[KeycloakError, IntrospectionResponse] = {
    val body = password ++ introspectionBody(token)
    val uri  = uri"${server.introspection_endpoint}"

    basicRequest
      .post(uri)
      .body(body)
      .response(asJson[IntrospectionResponse])
      .mapResponse(_.leftMap(_.toString))
      .send()
      .flatMap { r =>
        AuthzClient.liftM(r, buildRequestInfo(uri, "POST", body))
      }
      .mapError(KeycloakThrowable)
  }

  protected def withAuthNewToken[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID)
  : IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    issueAndSetAccessToken().map(tkn => request.auth.bearer(tkn.access))
  }

  def withAuth[A](request: RequestT[Identity, A, Nothing])(implicit cId: UUID): IO[KeycloakError, RequestT[Identity, A, Nothing]] = {
    evaluateToken().map(tkn => request.auth.bearer(tkn.access))
  }

  def authorize(request: AuthorizationRequest): IO[KeycloakError, TokenResponse] = {
    val body = Map(
      "ticket"             -> request.ticket,
      "claim_token"        -> request.claimToken,
      "claim_token_format" -> request.claimTokenFormat,
      "pct"                -> request.pct,
      "rpt"                -> request.rptToken,
      "scope"              -> request.scope,
      "audience"           -> request.audience.orElse(Some(config.authn.clientId)),
      "subject_token"      -> request.subjectToken
    )
      .collect { case (key, Some(value)) => key -> value :: Nil }

    val uri = uri"${server.token_endpoint}"
    basicRequest
      .post(uri)
      .body(body)
      .response(asJson[TokenResponse])
      .mapResponse(_.leftMap(_.toString))
      .send()
      .flatMap { r =>
        AuthzClient.liftM(r, buildRequestInfo(uri, "POST", body))
      }
      .mapError(KeycloakThrowable)
  }
}
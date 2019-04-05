package com.fullfacing.keycloak4s.monix.client

import java.nio.ByteBuffer
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

import com.fullfacing.keycloak4s.client.KeycloakConfig
import com.fullfacing.keycloak4s.models.{ErrorDump, RequestInfo}
import com.fullfacing.keycloak4s.monix.client.TokenManager.{Token, TokenResponse}
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp.{SttpBackend, _}
import monix.eval.Task
import monix.reactive.Observable
import org.json4s.Formats
import org.json4s.jackson.Serialization

abstract class TokenManager(config: KeycloakConfig)(implicit client: SttpBackend[Task, Observable[ByteBuffer]], formats: Formats) {

  protected implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  protected val F: MonadError[Task] = client.responseMonad

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

  protected def buildError(response: Response[_], requestInfo: RequestInfo): ErrorDump = {
    ErrorDump(
      code        = response.code,
      body        = response.body.fold(e => e, _ => "N/A"),
      headers     = response.headers,
      statusText  = response.statusText,
      requestInfo = requestInfo
    )
  }

  protected def liftM[A](response: Response[A], requestInfo: RequestInfo): Task[A] = response.body match {
    case Left(_)    => F.error(buildError(response, requestInfo))
    case Right(rsp) => F.unit(rsp)
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
  private def issueAccessToken(): Task[Token] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)

    val a = sttp.post(tokenEndpoint)
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    a.flatMap(liftM(_, requestInfo))
  }

  private def refreshAccessToken(t: Token): Task[Token] = {
    val requestInfo = buildRequestInfo(tokenEndpoint.path, "POST", password)

    val a = sttp.post(tokenEndpoint)
      .body(refresh(t))
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    a.flatMap(liftM(_, requestInfo))
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
  private def validateToken(): Task[Token] = {
    val token = ref.get()
    if (token == null) {
      issueAccessToken().map { nToken =>
        ref.set(nToken)
        nToken
      }
    } else {
      val epoch = Instant.now()

      if (epoch.isAfter(token.authenticateAt)) {
        issueAccessToken().map { nToken =>
          ref.set(nToken)
          nToken
        }
      } else if (epoch.isAfter(token.refreshAt)) {
        refreshAccessToken(token).map { nToken =>
          ref.set(nToken)
          nToken
        }
      } else {
        Task.pure(token)
      }
    }
  }

  /**
    *
    * @tparam A
    */
  def withAuth[A](request: RequestT[Id, A, Nothing]): Task[RequestT[Id, A, Nothing]] = {
    validateToken().map(a => request.auth.bearer(a.access))
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
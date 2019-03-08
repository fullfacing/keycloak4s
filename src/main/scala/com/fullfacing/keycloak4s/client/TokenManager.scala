package com.fullfacing.keycloak4s.client

import java.time.Instant

import cats.effect.Concurrent
import cats.effect.concurrent.MVar
import com.fullfacing.keycloak4s.client.TokenManager.{Token, TokenResponse}
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp.{SttpBackend, _}
import org.json4s.Formats
import org.json4s.native.Serialization

abstract class TokenManager[F[_] : Concurrent, -S](config: KeycloakConfig)(implicit client: SttpBackend[F, S], formats: Formats) {

  implicit val serialization: Serialization.type = org.json4s.native.Serialization

  private val authn = uri"http://${config.host}:${config.port}/auth/realms/${config.authn.realm}/protocol/openid-connect/token"

  private val credentials = Map(
    "username" -> config.authn.username,
    "password" -> config.authn.password,
    "client_id" -> config.authn.clientId,
    "grant_type" -> "password"
  )

  // Create the MVar and initialise it with an Access Token.
  private val token: F[MVar[F, Token]] = {
    Concurrent[F].flatMap(authenticate()) { response =>
      println(response)
      mapToToken(response) match {
        case Left(ex) =>
          println(ex)
          Concurrent[F].raiseError(new Throwable(ex))
        case Right(rs) => MVar[F].of(rs)
      }
    }
  }

  /**
    * Extract all the relevant data from the Keycloak Token Response.
    *
    * @param response the oidc token response.
    * @return a new token instance.
    */
  private def mapToToken(response: Response[TokenResponse]): Either[String, Token] = {
    response.body.map { tkn =>
      val instant = Instant.now()
      Token(
        access = tkn.access_token,
        refresh = tkn.refresh_token,
        refreshAt = instant.plusSeconds(tkn.expires_in),
        authenticateAt = instant.plusSeconds(tkn.refresh_expires_in)
      )
    }
  }

  /**
    * Authenticate the application with Keycloak, returning an access and refresh token.
    *
    * @return
    */
  private def authenticate(): F[Response[TokenResponse]] = {
    println(credentials)
    sttp.post(authn).body(credentials).mapResponse({s => println(s); s}).response(asJson[TokenResponse]).send()
  }

  /**
    *
    * @param f
    * @tparam A
    */
  def withAuth[A](request: RequestT[Id, A, Nothing]): F[RequestT[Id, A, Nothing]] = {
    println(request.uri)
    val t = Concurrent[F].flatMap(token)(_.read)
    Concurrent[F].map(t)(a => request.auth.bearer(a.access))
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
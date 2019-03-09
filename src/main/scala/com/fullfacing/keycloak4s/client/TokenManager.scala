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

  protected implicit val serialization: Serialization.type = org.json4s.native.Serialization

  protected val F: MonadError[F] = client.responseMonad

  protected def liftM[A](response: Either[String, A]): F[A] = response match {
    case Left(err) => F.error(new Throwable(err))
    case Right(rsp) => F.unit(rsp)
  }

  private val tokenEndpoint =
    uri"http://${config.host}:${config.port}/auth/realms/${config.authn.realm}/protocol/openid-connect/token"

  private val password = Map(
    "username" -> config.authn.username,
    "password" -> config.authn.password,
    "client_id" -> config.authn.clientId,
    "grant_type" -> "password"
  )



  // Create the MVar and initialise it with an Access Token.
  private val ref: F[MVar[F, Token]] = MVar.empty[F, Token]

  private def refresh(token: Token): Map[String, String] = Map(
    "client_id" -> config.authn.clientId,
    "refresh_token" -> token.refresh,
    "grant_type" -> "refresh_token"
  )

  /**
    * Authenticate the application with Keycloak, returning an access and refresh token.
    *
    * @return
    */
  private def issueAccessToken(): F[Token] = {
    val a = sttp.post(tokenEndpoint)
      .body(password)
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    Concurrent[F].flatMap(a)(aa => liftM(aa.body))
  }

  private def refreshAccessToken(t: Token): F[Token] = {
    val a = sttp.post(tokenEndpoint)
      .body(refresh(t))
      .response(asJson[TokenResponse])
      .mapResponse(mapToToken)
      .send()

    Concurrent[F].flatMap(a)(aa => liftM(aa.body))
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
    * @param t
    * @return
    */
  private def validateToken(ref: F[MVar[F, Token]]): F[Token] = {
    Concurrent[F].flatMap(ref) { mvar =>
      Concurrent[F].flatMap(mvar.isEmpty) { isEmpty =>
        if (isEmpty) issueAccessToken() else {
          val epoch = Instant.now()
          Concurrent[F].flatMap(mvar.read) { token =>
            if (token.authenticateAt.isAfter(epoch)) {
              Concurrent[F].flatMap(Concurrent[F].flatMap(mvar.take)(_ => issueAccessToken())) { t =>
                Concurrent[F].map(mvar.put(t))(_ => t)
              }
            } else if (token.refreshAt.isAfter(epoch)) {
              Concurrent[F].flatMap(Concurrent[F].flatMap(mvar.take)(_ => refreshAccessToken(token))) { t =>
                Concurrent[F].map(mvar.put(t))(_ => t)
              }
            } else {
              Concurrent[F].pure(token)
            }
          }
        }
      }
    }
  }

  /**
    *
    * @param f
    * @tparam A
    */
  def withAuth[A](request: RequestT[Id, A, Nothing]): F[RequestT[Id, A, Nothing]] = {
    Concurrent[F].map(validateToken(ref))(a => request.auth.bearer(a.access))
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
package com.fullfacing.keycloak4s.admin.models

import cats.implicits._
import sttp.client.ResponseError

import java.time.Instant

final case class TokenResponse(access_token: String,
                               expires_in: Long,
                               refresh_expires_in: Long,
                               refresh_token: Option[String] = None,
                               token_type: String,
                               `not-before-policy`: Int,
                               session_state: Option[String] = None,
                               scope: String)

object TokenResponse {

  /**
   * Extract all the relevant data from the Keycloak Token Response.
   *
   * @param response the oidc token response.
   * @return a new token instance.
   */
  def mapToToken(response: Either[ResponseError[Exception], TokenResponse]): Either[String, Token] = response.map { res =>
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
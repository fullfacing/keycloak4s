package com.fullfacing.keycloak4s.admin.utils

import com.fullfacing.keycloak4s.admin.models.TokenWithRefresh
import com.fullfacing.keycloak4s.core.models.KeycloakConfig
import com.fullfacing.keycloak4s.core.models.KeycloakConfig.Auth

object Credentials {

  def access(authn: Auth): Map[String, String] = authn match {
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

  def refresh(token: TokenWithRefresh, authn: Auth): Map[String, String] = authn match {
    case KeycloakConfig.Secret(_, clientId, clientSecret) =>
      Map(
        "client_id"     -> clientId,
        "refresh_token" -> token.refresh,
        "client_secret" -> clientSecret,
        "grant_type"    -> "refresh_token"
      )
    case _: KeycloakConfig.Password =>
      Map(
        "client_id"     -> authn.clientId,
        "refresh_token" -> token.refresh,
        "grant_type"    -> "refresh_token"
      )
    case KeycloakConfig.PasswordWithSecret(_, clientId, _, _, clientSecret) =>
      Map(
        "client_id"     -> clientId,
        "refresh_token" -> token.refresh,
        "client_secret" -> clientSecret,
        "grant_type"    -> "refresh_token"
      )
  }
}

package com.fullfacing.keycloak4s.core.models

final case class KeycloakConfig(scheme: String,
                                host: String,
                                port: Int,
                                realm: String,
                                authn: KeycloakConfig.Auth)


object KeycloakConfig {

  sealed trait Auth {
    val realm: String
    val clientId: String
  }

  final case class Password(realm: String, clientId: String, username: String, password: String) extends Auth

  final case class Secret(realm: String, clientId: String, clientSecret: String) extends Auth
}

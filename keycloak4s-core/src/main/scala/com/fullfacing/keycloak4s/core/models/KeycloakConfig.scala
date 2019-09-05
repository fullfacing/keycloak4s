package com.fullfacing.keycloak4s.core.models

trait KeycloakConfig {
  val scheme: String
  val host: String
  val port: Int
  val realm: String
}

final case class ConfigWithAuth(scheme: String,
                                host: String,
                                port: Int,
                                realm: String,
                                authn: KeycloakConfig.Auth) extends KeycloakConfig

final case class ConfigWithoutAuth(scheme: String,
                                   host: String,
                                   port: Int,
                                   realm: String) extends KeycloakConfig

object KeycloakConfig {

  sealed trait Auth {
    val realm: String
    val clientId: String
  }

  final case class Password(realm: String, clientId: String, username: String, password: String) extends Auth

  final case class Secret(realm: String, clientId: String, clientSecret: String) extends Auth
}

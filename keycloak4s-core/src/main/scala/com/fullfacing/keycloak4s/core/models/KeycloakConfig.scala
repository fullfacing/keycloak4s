package com.fullfacing.keycloak4s.core.models

final case class KeycloakConfig(scheme: String,
                                host: String,
                                port: Int,
                                realm: String,
                                authn: KeycloakConfig.Auth)


object KeycloakConfig {

  final case class Auth(realm: String,
                        clientId: String,
                        clientSecret: String)
}

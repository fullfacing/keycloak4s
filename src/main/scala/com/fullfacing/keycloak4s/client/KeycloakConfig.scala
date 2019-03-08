package com.fullfacing.keycloak4s.client

final case class KeycloakConfig(scheme: String = "http",
                                host: String = "localhost",
                                port: Int = 8080,
                                realm: String = "admin",
                                authn: KeycloakConfig.Auth)


object KeycloakConfig {

  final case class Auth(realm: String,
                        username: String,
                        password: String,
                        clientId: String)

}
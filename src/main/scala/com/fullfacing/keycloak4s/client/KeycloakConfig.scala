package com.fullfacing.keycloak4s.client

case class KeycloakConfig(scheme: String = "http",
                          host: String = "localhost",
                          port: Int = 8080,
                          realm: String = "admin")

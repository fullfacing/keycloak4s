package com.fullfacing.keycloak4s.handles

case class KeycloakConfig(scheme: String = "http",
                          host: String = "localhost",
                          port: Int = 8080,
                          realm: String = "admin")

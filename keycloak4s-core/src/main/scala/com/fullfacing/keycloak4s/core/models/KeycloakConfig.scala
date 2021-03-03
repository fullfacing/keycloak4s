package com.fullfacing.keycloak4s.core.models

sealed trait KeycloakConfig {
  val scheme: String
  val host: String
  val port: Int
  val realm: String
  val basePath: List[String]
  val proxyUrl: Option[String]

  def buildBaseUri: String = s"$scheme://$host:$port" + (if (basePath.nonEmpty) s"/${basePath.mkString("/")}" else "")
}

final case class ConfigWithAuth(scheme: String,
                                host: String,
                                port: Int,
                                realm: String,
                                authn: KeycloakConfig.Auth,
                                proxyUrl: Option[String] = None,
                                basePath: List[String] = List("auth")) extends KeycloakConfig

final case class ConfigWithoutAuth(scheme: String,
                                   host: String,
                                   port: Int,
                                   realm: String,
                                   proxyUrl: Option[String] = None,
                                   basePath: List[String] = List("auth")) extends KeycloakConfig

object KeycloakConfig {

  sealed trait Auth {
    val realm: String
    val clientId: String
  }

  final case class Secret(realm: String, clientId: String, clientSecret: String) extends Auth

  final case class Password(realm: String, clientId: String, username: String, password: String) extends Auth

  final case class PasswordWithSecret(realm: String,
                                      clientId: String,
                                      username: String,
                                      password: String,
                                      clientSecret: String) extends Auth
}

package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.authorization.{NodeAuthorization, PathAuthorization, PolicyBuilders}

object Config {

  lazy val apiSecurityConfig: NodeAuthorization = PolicyBuilders.buildNodeAuthorization("config.json")

  lazy val nodeClientsConfig: NodeAuthorization = PolicyBuilders.buildNodeAuthorization("clients_configA.json")

  val pathClientsConfig: PathAuthorization = PolicyBuilders.buildPathAuthorization("clients_configB.json")
}

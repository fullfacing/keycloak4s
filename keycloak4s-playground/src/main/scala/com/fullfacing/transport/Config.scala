package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.authorization.{NodeAuthorization, PathAuthorization, PolicyEnforcement}

object Config {

  lazy val apiSecurityConfig: NodeAuthorization = PolicyEnforcement.buildNodeAuthorization("config.json")

  lazy val nodeClientsConfig: NodeAuthorization = PolicyEnforcement.buildNodeAuthorization("clients_configA.json")

  val pathClientsConfig: PathAuthorization = PolicyEnforcement.buildPathAuthorization("clients_configB.json")
}

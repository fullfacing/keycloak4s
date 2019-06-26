package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.authorisation.{NodeAuthorisation, PathAuthorisation, PolicyEnforcement}

object Config {

  lazy val apiSecurityConfig: NodeAuthorisation = PolicyEnforcement.buildNodeAuthorisation("config.json")

  lazy val nodeClientsConfig: NodeAuthorisation = PolicyEnforcement.buildNodeAuthorisation("clients_configA.json")

  val pathClientsConfig: PathAuthorisation = PolicyEnforcement.buildPathAuthorisation("clients_configB.json")
}

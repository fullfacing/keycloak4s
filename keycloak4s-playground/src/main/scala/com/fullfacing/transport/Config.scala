package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.authorization.{PathAuthorization, PolicyBuilders}

object Config {

  val pathClientsConfig: PathAuthorization = PolicyBuilders.buildPathAuthorization("clients_configB.json")
}

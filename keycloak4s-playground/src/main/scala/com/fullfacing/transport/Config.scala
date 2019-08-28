package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.core.PolicyBuilders
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization

object Config {

  val pathClientsConfig: PathAuthorization = PolicyBuilders.buildPathAuthorization("clients_configB.json")
}

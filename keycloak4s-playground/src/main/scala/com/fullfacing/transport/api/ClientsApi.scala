package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.transport.Implicits._
import com.fullfacing.transport.Config._

object ClientsApi extends SecurityDirectives {

  val api: Route = secure(pathClientsConfig) {
    ClientsRoutes.api ~
      AccountRoutes.api ~
      SiteRoutes.api
  }
}

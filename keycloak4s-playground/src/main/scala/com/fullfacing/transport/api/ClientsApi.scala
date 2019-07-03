package com.fullfacing.transport.api

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.transport.Config._
import com.fullfacing.transport.Implicits._

object ClientsApi extends SecurityDirectives {

  val api: Route =
    secure(pathClientsConfig) {
      ClientsRoutes.api ~
        AccountRoutes.api ~
        SiteRoutes.api
    }


  def context: Directive1[UUID] = {
    optionalHeaderValueByName("Postman-Token").flatMap(cId => provide(cId.map(UUID.fromString).getOrElse(UUID.randomUUID())))
  }
}

package com.fullfacing.transport.api

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.transport.Config._
import com.fullfacing.transport.Implicits._

object ClientsApi extends SecurityDirectives {

  val api: Route =
    context { correlationId =>
      secure((pathClientsConfig, correlationId)) {
        ClientsRoutes.api ~
          AccountRoutes.api ~
          SiteRoutes.api
      }
    }

<<<<<<< HEAD
  def contextFromPostman: Directive1[UUID] = {
    optionalHeaderValueByName("Postman-Token").flatMap { cId =>
      provide {
        cId.fold(UUID.randomUUID())(UUID.fromString)
      }
    }
=======

  def context: Directive1[UUID] = {
    optionalHeaderValueByName("Postman-Token")
      .flatMap(cId => provide(cId.map(UUID.fromString).getOrElse(UUID.randomUUID())))
>>>>>>> e4e1a9e0a9fd9a32681ae47623dfd421e7b20720
  }
}

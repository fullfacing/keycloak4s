package com.fullfacing.transport.api

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirective._
import com.fullfacing.transport.Config._
import com.fullfacing.transport.Implicits._

object ClientsApi {

  lazy val api: Route =
    contextFromPostman { correlationId =>
      secure((pathClientsConfig, correlationId)) { _ =>
          SiteRoutes.api
      }
    }

  def contextFromPostman: Directive1[UUID] = {
    optionalHeaderValueByName("Postman-Token").flatMap { cId =>
      provide {
        cId.fold(UUID.randomUUID())(UUID.fromString)
      }
    }
  }
}
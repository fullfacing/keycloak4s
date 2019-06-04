package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object ClientsRoutes {

  val api: Route = pathPrefix("clients") {
    get {
      pathEndOrSingleSlash {
        complete("GET /clients")
      } ~
        pathPrefix(JavaUUID) { id =>
          pathEndOrSingleSlash {
            complete(s"GET /clients/$id")
          } ~
            path("accounts") {
              complete(s"GET /clients/$id/accounts")
            } ~
            path("sites") {
              complete(s"GET /clients/$id/sites")
            } ~
            path("overview") {
              complete(s"GET /clients/$id/overview")
            }
        }
    } ~
      post {
        pathEndOrSingleSlash {
          complete("POST /clients")
        } ~
          pathPrefix(JavaUUID) { id =>
            path("activate") {
              complete(s"POST /clients/$id/activate")
            } ~
              path("deactivate") {
                complete(s"POST /clients/$id/deactivate")
              } ~
              path("datastream" / "on") {
                complete(s"POST /clients/$id/datastream/on")
              } ~
              path("datastream" / "off") {
                complete(s"POST /clients/$id/datastream/off")
              }
          }
      } ~
      patch {
        path(JavaUUID) { id =>
          complete(s"PATCH /clients/$id")
        }
      } ~
      delete {
        path(JavaUUID) { id =>
          complete(s"DELETE /clients/$id")
        }
      }
  }
}

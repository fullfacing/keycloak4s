package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object AccountRoutes {

  val api: Route = pathPrefix("accounts") {
    get {
      pathEndOrSingleSlash {
        complete("GET /accounts")
      } ~
        pathPrefix(JavaUUID) { id =>
          pathEndOrSingleSlash {
            complete(s"GET /accounts/$id")
          } ~
            path("sites") {
              complete(s"GET /accounts/$id/sites")
            } ~
            path("overview") {
              complete(s"GET /accounts/$id/overview")
            }
        }
    } ~
      post {
        pathEndOrSingleSlash {
          complete("POST /accounts")
        } ~
          pathPrefix(JavaUUID) { id =>
            path("activate") {
              complete(s"POST /accounts/$id/activate")
            } ~
              path("deactivate") {
                complete(s"POST /accounts/$id/deactivate")
              } ~
              path("datastream" / "on") {
                complete(s"POST /accounts/$id/datastream/on")
              } ~
              path("datastream" / "off") {
                complete(s"POST /accounts/$id/datastream/off")
              }
          }
      } ~
      patch {
        path(JavaUUID) { id =>
          complete(s"PATCH /accounts/$id")
        }
      } ~
      delete {
        path(JavaUUID) { id =>
          complete(s"DELETE /accounts/$id")
        }
      }
  }
}


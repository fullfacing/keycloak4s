package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object SiteRoutes {

  val api: Route = pathPrefix("sites") {
    get {
      pathEndOrSingleSlash {
        complete("GET /sites")
      } ~
        pathPrefix(JavaUUID) { id =>
          pathEndOrSingleSlash {
            complete(s"GET /sites/$id")
          }
        }
    } ~
      post {
        pathEndOrSingleSlash {
          complete("POST /sites")
        } ~
          pathPrefix(JavaUUID) { id =>
            path("activate") {
              complete(s"POST /sites/$id/activate")
            } ~
              path("deactivate") {
                complete(s"POST /sites/$id/deactivate")
              } ~
              path("datastream" / "on") {
                complete(s"POST /sites/$id/datastream/on")
              } ~
              path("datastream" / "off") {
                complete(s"POST /sites/$id/datastream/off")
              }
          }
      } ~
      patch {
        path(JavaUUID) { id =>
          complete(s"PATCH /sites/$id")
        }
      } ~
      delete {
        path(JavaUUID) { id =>
          complete(s"DELETE /sites/$id")
        }
      }
  }
}

package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.transport.Config._
import com.fullfacing.transport.Implicits._


object TransportAPI extends SecurityDirectives {

  val api: Route = {
    pathPrefix("transport") {
      secure(apiSecurityConfig) {
        cars ~ bikes ~ buses
      }
    }
  }

  def cars: Route = pathPrefix("cars") {
    get {
      pathEndOrSingleSlash {
        complete("GET /cars")
      } ~
      path(JavaUUID) { id =>
        complete(s"GET /cars/$id")
      }
    } ~
    put {
      path(JavaUUID) { id =>
        complete(s"PUT /cars/$id")
      }
    } ~
    post {
      complete("POST /cars")
    } ~
    patch {
      path(JavaUUID) { id =>
        complete(s"PATCH /cars/$id")
      }
    } ~
    delete {
      complete("DELETE /cars")
    }
  }

  def bikes: Route = pathPrefix("bikes") {
    get {
      pathEndOrSingleSlash {
        complete("GET /bikes")
      } ~
        path(JavaUUID) { id =>
          complete(s"GET /bikes/$id")
        }
    } ~
    put {
      path(JavaUUID) { id =>
        complete(s"PUT /bikes/$id")
      }
    } ~
    post {
      complete("POST /bikes")
    } ~
    patch {
      path(JavaUUID) { id =>
        complete(s"PATCH /bikes/$id")
      }
    } ~
    delete {
      complete("DELETE /bikes")
    }
  }

  def buses: Route = pathPrefix("buses") {
    get {
      pathEndOrSingleSlash {
        complete("GET /buses")
      } ~
      path(JavaUUID) { id =>
        complete(s"GET /buses/$id")
      }
    } ~
    put {
      path(JavaUUID) { id =>
        complete(s"PUT /buses/$id")
      }
    } ~
    post {
      complete("POST /buses")
    } ~
    patch {
      path(JavaUUID) { id =>
        complete(s"PATCH /buses/$id")
      }
    } ~
    delete {
      complete("DELETE /buses")
    }
  }

}


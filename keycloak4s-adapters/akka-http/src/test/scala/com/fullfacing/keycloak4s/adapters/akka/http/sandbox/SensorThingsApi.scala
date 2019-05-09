package com.fullfacing.keycloak4s.adapters.akka.http.sandbox

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.adapters.akka.http.directives.SecurityDirectives
import com.fullfacing.keycloak4s.adapters.akka.http.sandbox.Implicits._

object SensorThingsApi extends SecurityDirectives {

  val api: Route = secure(apiSecurityConfig) {
    pathPrefix("sensorthings") {
      thing ~ obs ~ sensor ~ ds
    }
  }

  def thing: Route = pathPrefix("things") {
    pathEndOrSingleSlash {
      complete("/things GetByQuery")
    } ~
    pathPrefix(JavaUUID) { id =>
      pathEndOrSingleSlash {
        complete(s"/things GetById $id")
      } ~
      path("datastreams") {
        complete(s"/things/$id/datastreams GetByQuery")
      }
    }
  }

  def obs: Route = pathPrefix("observations") {
    pathEndOrSingleSlash {
      complete("/observations GetByQuery")
    } ~
    pathPrefix(JavaUUID) { id =>
      pathEndOrSingleSlash {
        complete(s"/observations GetById $id")
      } ~
      path("featureofinterests") {
        complete(s"/observations/$id/featureofinterests GetByQuery")
      }
    }
  }

  def sensor: Route = pathPrefix("sensors") {
    pathEndOrSingleSlash {
      complete("/sensors GetByQuery")
    } ~
    pathPrefix(JavaUUID) { id =>
      pathEndOrSingleSlash {
        complete(s"/sensors GetById $id")
      } ~
      path("datastreams") {
        complete(s"/sensors/$id/datastreams GetByQuery")
      }
    }
  }

  def ds: Route = pathPrefix("datastreams") {
    pathEndOrSingleSlash {
      complete("/datastreams GetByQuery")
    } ~
    pathPrefix(JavaUUID) { id =>
      pathEndOrSingleSlash {
        complete(s"/datastreams GetById $id")
      } ~
      path("observations") {
        complete(s"/datastreams/$id/observations GetByQuery")
      }
    }
  }
}

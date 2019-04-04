package com.fullfacing.keycloak4s.adapters.akka.http.apollo

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.apollo.core.health.HealthCheck
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.Directives.{context, validateToken}
import com.fullfacing.apollo.http.rest.BaseUri
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.BaseRoutesWithAuth.RequestHandler

abstract class BaseRoutesWithAuth(uri: BaseUri) {

  lazy val routes: Route = uri.render {
    path("healthz") {
      get {
        complete("")
      }
    } ~
    context { initial =>
      validateToken(initial) { context =>
        api(context)
      }
    }
  }

  // The custom API definition for this route.
  val api: RequestHandler

  /**
   * A list of resource handles that should be checked for availability. This is used to determine whether or not
   * an application is able to handle requests.
   *
   * An application is only considered 'available' if all it's resources handles are usable, an example of this is
   * database connections.
   */
  val resources: List[HealthCheck]
}

object BaseRoutesWithAuth {
  type RequestHandler = RequestContext => Route
}

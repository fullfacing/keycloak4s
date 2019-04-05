package com.fullfacing.keycloak4s.adapters.akka.http.apollo

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.apollo.core.health.HealthCheck
import com.fullfacing.apollo.http.rest.BaseUri
import com.fullfacing.keycloak4s.adapters.akka.http.TokenValidator
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.BaseRoutesWithAuth.RequestHandler
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives.Directives.{context, validateToken}
import monix.execution.Scheduler

abstract class BaseRoutesWithAuth(uri: BaseUri)(implicit tv: TokenValidator, s: Scheduler) {

  lazy val routes: Route = uri.render {
    path("healthz") {
      get {
        complete("")
      }
    } ~
    context { initial =>
      validateToken(initial)(tv, s) { context =>
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

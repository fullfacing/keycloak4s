package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.server.{Directive, PathMatcher}
import akka.http.scaladsl.server.Directives._
import com.fullfacing.apollo.core.health.HealthCheck
import com.fullfacing.apollo.http.rest.BaseUri._
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.BaseRoutesWithAuth.RequestHandler
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.BaseRoutesWithAuth
import com.fullfacing.keycloak4s.adapters.akka.http.Implicits._
import com.fullfacing.keycloak4s.adapters.akka.http.apollo.directives.Directives._

object HttpApi extends BaseRoutesWithAuth("test" - "adapter") {

  val resources: List[HealthCheck] = List.empty

  def resourcePath[L](pm: PathMatcher[L]): Directive[L] = pathPrefix(pm ~ PathEnd)

  override val api: RequestHandler = { implicit ctx =>
    get {
      path("cars") {
        authorise("cars")(ctx) {
          complete(s"This is an auth test \n $ctx")
        }
      } ~
      pathPrefix("planes") {
        path("models") {
          authorise("planes", "models")(ctx) {
            complete(s"This is an auth test \n $ctx")
          }
        } ~
        path("pilots") {
          authorise("planes", "pilots")(ctx) {
            complete(s"This is an auth test \n $ctx")
          }
        }
      }
    }
  }
}

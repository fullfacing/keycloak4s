package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, Route}
import com.fullfacing.keycloak4s.adapters.akka.http.Implicits._
import com.fullfacing.keycloak4s.adapters.akka.http.directives.{AuthorisationDirectives, ValidationDirective}

object HttpApi extends ValidationDirective with AuthorisationDirectives {

  val api: Route = validateToken(tv) { implicit permissions =>
    path("cars") { m =>
      get(m) {
        complete(s"GET /cars \n $permissions")
      } ~
      post(m) {
        complete(s"POST /cars \n $permissions")
      } ~
      put(m) {
        complete(s"PUT /cars \n $permissions")
      } ~
      patch(m) {
        complete(s"PATCH /cars \n $permissions")
      } ~
      delete(m) {
        complete(s"DELETE /cars \n $permissions")
      }
    } ~
      path((JavaUUID, "cars")) { (id, m) =>
        complete(s"GetById $id | $m")
      }
  }
}
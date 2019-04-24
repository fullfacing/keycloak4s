package com.fullfacing.keycloak4s.adapters.akka.http.sandbox

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.adapters.akka.http.sandbox.Implicits._
import com.fullfacing.keycloak4s.adapters.akka.http.directives.{AuthorisationDirectives, ValidationDirective}

object HttpApi extends ValidationDirective with AuthorisationDirectives {

  val api: Route = pathPrefix("test") {
    validateToken(tv) { permissions =>
      path("cars") {
        getA(permissions) {
          withAuth("cars", permissions) {
            complete(s"GET /cars \n $permissions")
          }
        } ~
        postA(permissions) {
          withAuth("cars", permissions) {
            complete(s"POST /cars \n $permissions")
          }
        } ~
        putA(permissions) {
          withAuth("cars", permissions) {
            complete(s"PUT /cars \n $permissions")
          }
        } ~
        patchA(permissions) {
          withAuth("cars", permissions) {
            complete(s"PATCH /cars \n $permissions")
          }
        } ~
        deleteA(permissions) {
          withAuth("cars", permissions) {
            complete(s"DELETE /cars \n $permissions")
          }
        } ~
        deleteA(permissions) {
          withAuth("cars", permissions) {
            complete(s"DELETE /cars \n $permissions")
          }
        }
      }
    }
  }
}

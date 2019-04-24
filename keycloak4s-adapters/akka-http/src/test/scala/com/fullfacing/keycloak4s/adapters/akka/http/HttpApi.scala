package com.fullfacing.keycloak4s.adapters.akka.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.adapters.akka.http.Implicits._
import com.fullfacing.keycloak4s.adapters.akka.http.directives.SecurityDirectives

object HttpApi extends SecurityDirectives {

  val api: Route = secure("fng-api-test") { implicit permissions =>
    pathA("cars") { roles =>
      getA(roles) {
        complete(s"GET /cars \n $permissions")
      } ~
      postA(roles) {
        complete(s"POST /cars \n $permissions")
      } ~
      putA(roles) {
        complete(s"PUT /cars \n $permissions")
      } ~
      patchA(roles) {
        complete(s"PATCH /cars \n $permissions")
      } ~
      deleteA(roles) {
        complete(s"DELETE /cars \n $permissions")
      }
    }
  }


  val api2: Route = secure("fng-api-test") { implicit permissions =>
    pathA((JavaUUID, "cars")) { (m, id) =>
      complete(s"GetById $id | $m")
    } ~
    pathA(("cars", JavaUUID)) { (m, id) =>
      getA(m) {
        complete(s"$id")
      }
    } ~
    path("cars") {
      get {
        withAuth("cars") {
          complete("")
        }
      }
    }
  }
}
package com.fullfacing.transport.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.transport.Implicits._

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
    pathA(JavaUUID, "cars") { (roles, id) =>
      getA(roles) {
        complete(s"$id | $roles")
      }
    } ~
    pathA("cars", JavaUUID) { (roles, id) =>
      getA(roles) {
        complete(s"GetById $id | $roles")
      }
    } ~
    path("cars") {
      get {
        withAuth("cars") {
          complete("Authorised")
        }
      }
    }
  }

  val api3: Route = secure("fng-api-test") { implicit permissions =>
    path("cars") {
      authoriseResource("cars") { roles =>
        get {
          authoriseMethod(roles) {
            complete("Get /cars")
          }
        } ~
        post {
          authoriseMethod(roles) {
            complete("Post /cars")
          }
        }
      }
    }
  }

  val api4: Route = secure("fng-api-test") { implicit permissions =>
    pathPrefixA("cars") { roles =>
      getA(roles) {
        path(JavaUUID) { id =>
          complete(s"GetById $id")
        } ~
        complete("GetByQuery")
      } ~
      deleteA(roles) {
        path(JavaUUID) { id =>
          complete(s"Delete $id")
        }
      }
    }
  }
}

package com.fullfacing.keycloak4s.sandbox

import com.fullfacing.keycloak4s.services.Authenticate
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}

import scala.concurrent.duration.FiniteDuration

object TestTokenManager {

  implicit val scheduler: Scheduler = Scheduler.fixedPool(s"device-io", 200)

  private var accessToken  = ""
  private var refreshToken = ""

  implicit def getAuthToken: String = accessToken

  def getInitialToken(realm: String, client: String, username: String, password: String): Task[Cancelable] = Task.eval {
    scheduler.scheduleOnce(FiniteDuration(5, "seconds")){
      println("\nGet Token\n")
      Authenticate.getAccessToken(realm, username, password, client).foreach(_.foreach { response =>
        println(response)
        accessToken = response.access_token
        refreshToken = response.refresh_token
      })
    }
  }

  def refreshToken(realm: String, client: String): Task[Cancelable] = Task.eval {
    scheduler.scheduleAtFixedRate(FiniteDuration(55, "seconds"), FiniteDuration(55, "seconds")) {
      println("\nRefresh Token: \n")
      Authenticate.refreshAccessToken(realm, refreshToken, client).foreach(_.foreach { response =>
        println(response)
        accessToken = response.access_token
        refreshToken = response.refresh_token
      })
    }
  }

}

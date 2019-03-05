package com.fullfacing.keycloak4s.sandbox

import com.fullfacing.keycloak4s.services.Authenticate
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}

import scala.concurrent.duration._

object TestTokenManager {

  /* PLEASE NOTE: THIS CLASS IS FOR LOCAL TESTING ONLY (@rich please ignore the vars) **/

  implicit val scheduler: Scheduler = Scheduler.fixedPool(s"scheduler", 200)

  @volatile
  private var accessToken = ""

  @volatile
  private var refreshToken = ""

  implicit def getAuthToken: String = accessToken

  def getInitialToken(realm: String, client: String, username: String, password: String): Task[Cancelable] = Task.eval {
    scheduler.scheduleOnce(5.seconds){
      Authenticate.getAccessToken(realm, username, password, client).foreach(_.foreach { response =>
        accessToken = response.access_token
        refreshToken = response.refresh_token
      })
    }
  }

  def refreshToken(realm: String, client: String): Task[Cancelable] = Task.eval {
    scheduler.scheduleAtFixedRate(55.seconds, 55.seconds) {
      Authenticate.refreshAccessToken(realm, refreshToken, client).foreach(_.foreach { response =>
        accessToken = response.access_token
        refreshToken = response.refresh_token
      })
    }
  }
}

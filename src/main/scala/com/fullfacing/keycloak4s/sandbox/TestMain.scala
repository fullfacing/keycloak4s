package com.fullfacing.keycloak4s.sandbox

import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.sandbox.TestTokenManager._
import com.fullfacing.keycloak4s.services._
import monix.execution.Cancelable

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object TestMain extends App {

  def testFunction(): Cancelable = {
    scheduler.scheduleOnce(FiniteDuration(15, "seconds")){
      AttackDetection.getUserStatus("demo", "ffc81e94-bc3d-422a-b4a0-71d517218d9c")
        .onErrorHandle { ex => ex.printStackTrace(); Left(ErrorPayload(500, "")) }
        .foreach(_.foreach(_.foreach(println)))
    }
  }

  val task = startAuthenticationService("master", "admin-cli", "lmuller", "keycloaktest")

  testFunction()

  Await.ready(task.runToFuture, Duration.Inf)
}

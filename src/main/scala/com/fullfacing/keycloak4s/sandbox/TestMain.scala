package com.fullfacing.keycloak4s.sandbox

import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.sandbox.TestTokenManager._
import com.fullfacing.keycloak4s.services.AttackDetection
import monix.eval.Task
import monix.execution.Cancelable

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object TestMain extends App {

  def startAuthenticationService(realm: String, client: String, username: String, password: String): Task[Unit] =
    for {
      _ <- getInitialToken(realm, client, username, password)
      _ <- refreshToken(realm, client)
    } yield run()

  def run(): Unit = {
    Thread.sleep(10000)
    run()
  }

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

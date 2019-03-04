package com.fullfacing.keycloak4s

import com.fullfacing.keycloak4s.TestTokenManager._
import com.fullfacing.keycloak4s.services.Users
import monix.eval.Task
import monix.execution.Cancelable

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object Main extends App {

  def startAuthenticationService(realm: String, client: String, username: String, password: String): Task[Unit] =
    for {
      _ <- getInitialToken(realm, client, username, password)
      _ <- refreshToken(realm, client)
    } yield run()

  def run(): Unit = {
    Thread.sleep(10000)
    run()
  }

  def testFunction: Cancelable = {
    scheduler.scheduleOnce(FiniteDuration(10, "seconds")){
      Users.getUsers("demo").foreach(_.foreach(_.foreach(println)))
    }
  }

  val task = startAuthenticationService("master", "admin-cli", "sjameson", "SmsQnMJUpVhgQ4U")

  testFunction

  Await.ready(task.runToFuture, Duration.Inf)
}
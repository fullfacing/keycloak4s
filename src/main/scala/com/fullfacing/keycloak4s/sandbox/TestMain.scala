package com.fullfacing.keycloak4s.sandbox

import com.fullfacing.keycloak4s.sandbox.TestTokenManager._
import monix.eval.Task

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object TestMain extends App {
  /* Main for testing, uncomment Await.ready to execute authentication service. **/
  def startAuthenticationService(realm: String, client: String, username: String, password: String): Task[Unit] =
    for {
      _ <- getInitialToken(realm, client, username, password)
      _ <- refreshToken(realm, client)
    } yield StdIn.readLine()

  val task = startAuthenticationService("master", "admin-cli", "lmuller", "keycloaktest")

  //Await.ready(task.runToFuture, Duration.Inf)
}

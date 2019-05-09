package com.fullfacing.keycloak4s.adapters.akka.http.sandbox

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import monix.eval.Task
import monix.execution.Scheduler.global

object Akka {
  implicit val sys: ActorSystem = ActorSystem("ActorSystem", defaultExecutionContext = Some(global))
  implicit val mat: ActorMaterializer = ActorMaterializer()

  def connect(): Task[Unit] = Task.deferFutureAction { implicit ctx =>
    Http().bindAndHandle(SensorThingsApi.api, "0.0.0.0", 8192).map { binding =>
      println(s"Bind Started: \n $binding")
    }
  }
}

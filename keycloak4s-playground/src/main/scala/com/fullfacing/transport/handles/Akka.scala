package com.fullfacing.transport.handles

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.fullfacing.transport.api.ClientsApi
import monix.eval.Task
import monix.execution.Scheduler.global

object Akka {
  implicit val sys: ActorSystem = ActorSystem("ActorSystem", defaultExecutionContext = Some(global))

  def connect(): Task[Unit] = Task.deferFutureAction { implicit ctx =>
    Http()
      .newServerAt("0.0.0.0", 8192)
      .bind(ClientsApi.api)
      .map(b => println(s"Bind Started: \n $b"))
  }
}

package com.fullfacing.keycloak4s.adapters.akka.http

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler
import org.json4s.Formats

object Main extends TaskApp {
  implicit val formats: Formats = org.json4s.DefaultFormats
  implicit val s: Scheduler = monix.execution.Scheduler.global

  override def run(args: List[String]): Task[ExitCode] = {
    Task.eval(ExitCode.Success)
  }.onErrorHandle{ex => ex.printStackTrace(); ExitCode.Error}
}

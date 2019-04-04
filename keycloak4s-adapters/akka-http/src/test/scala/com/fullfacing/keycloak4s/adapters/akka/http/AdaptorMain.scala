package com.fullfacing.keycloak4s.adapters.akka.http

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}

object AdaptorMain extends TaskApp {

  def run(args: List[String]): Task[ExitCode] = Akka.connect().map(_ => ExitCode.Success)
}

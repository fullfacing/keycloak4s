package com.fullfacing.keycloak4s.adapters.akka.http

import monix.execution.Scheduler

object Implicits {
  implicit val scheduler: Scheduler = Scheduler.io("adaptor-test-io")
  implicit val tv: TokenValidator = new TokenValidator("localhost", "8080", "master")
}

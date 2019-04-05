package com.fullfacing.keycloak4s.adapters.akka.http

import java.net.URL

import cats.implicits._
import com.nimbusds.jose.jwk.JWKSet
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.atomic.Atomic

import scala.concurrent.Future
import scala.util.control.NonFatal

class JWKSCache(host: String, port: String, realm: String)(implicit s: Scheduler) {
  private type NullFuture = Future[Either[Throwable, JWKSet]]

  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  private val task = Task {
    try JWKSet.load(url).asRight catch { case NonFatal(e) => e.asLeft }
  }.onErrorHandle(_.asLeft)

  private val future = Atomic(null: NullFuture)

  private def updateAndGet(retryCount: Int = 0): Future[Either[Throwable, JWKSet]] = synchronized {
    future.set(task.runToFuture)
    future.get
  }

  lazy val keySet: Task[Either[Throwable, JWKSet]] = Task.deferFuture {
    future.get match {
      case null   => updateAndGet()
      case valid  => valid
    }
  }

  def reobtainKeys(): Task[Either[Throwable, JWKSet]] = {
    future.set(null: NullFuture)
    keySet
  }
}

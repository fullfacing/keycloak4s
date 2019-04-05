package com.fullfacing.keycloak4s.adapters.akka.http

import java.net.URL

import cats.implicits._
import com.nimbusds.jose.jwk.JWKSet
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.atomic.Atomic

import scala.concurrent.Future

class JwksCache(host: String, port: String, realm: String)(implicit s: Scheduler) {
  private type NullFuture = Future[Either[Throwable, JWKSet]]

  /* The URL to retrieve the ConnectID JWKS **/
  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The asynchronous Task to retrieve the JWKSet. **/
  private val task = Task {
    JWKSet.load(url).asRight[Throwable]
  }.onErrorHandle(_.asLeft)

  /* The cached Future resulting from executing the JWKSet retrieval Task. **/
  private val future = Atomic(null: NullFuture)

  /* Executes the JWKSet retrieval Task, caches the resulting Future and returns it. **/
  private def updateAndGet(retryCount: Int = 0): Future[Either[Throwable, JWKSet]] = synchronized {
    future.set(task.runToFuture)
    future.get
  }

  /* The Task containing the currently cached JWKSet. **/
  lazy val keySet: Task[Either[Throwable, JWKSet]] = Task.deferFuture {
    future.get match {
      case null   => updateAndGet()
      case valid  => valid
    }
  }

  /* Drops the cache, re-executes the JWKSet retrieval Task and returns the result. **/
  def reobtainKeys(): Task[Either[Throwable, JWKSet]] = {
    future.set(null: NullFuture)
    keySet
  }
}

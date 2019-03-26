package com.fullfacing.keycloak4s.monix

import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.keycloak4s.client.KeycloakConfig
import com.fullfacing.keycloak4s.monix.client.{Keycloak, KeycloakClient}
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.json4s.jackson.Serialization.writePretty
import org.json4s.Formats

import scala.language.postfixOps

object MainMonix extends App {

  implicit val formats: Formats = org.json4s.DefaultFormats
  implicit val sttpBackend: MonixHttpBackendL = new MonixHttpBackendL(AsyncHttpClientMonixBackend())

  val config = KeycloakConfig("http", "localhost", 8080, "master", KeycloakConfig.Auth("master", "admin-cli", "6808820a-b662-4480-b832-f2d024eb6e03"))


  implicit val client: KeycloakClient =
    new KeycloakClient(config)


  val clients = Keycloak.Keys
  import scala.concurrent.duration._
  global.scheduleOnce(0 seconds) {
    clients.getRealmKeys().foreachL(s => println(writePretty(s))).onErrorHandle(_.printStackTrace()).runToFuture
  }



  Console.readBoolean()
}

class MonixHttpBackendL(delegate: SttpBackend[Task, Observable[ByteBuffer]]) extends SttpBackend[Task, Observable[ByteBuffer]] {
  override def send[T](request: Request[T, Observable[ByteBuffer]]): Task[Response[T]] =
    delegate.send(request)

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[Task] = new MonadError[Task] {
    override def unit[T](t: T): Task[T] =
      Task.now(t)

    override def map[T, T2](fa: Task[T])(f: T => T2): Task[T2] =
      fa.map(f)

    override def flatMap[T, T2](fa: Task[T])(f: T => Task[T2]): Task[T2] =
      fa.flatMap(f)

    override def error[T](t: Throwable): Task[T] =
      Task.raiseError(t)

    override protected def handleWrappedError[T](rt: Task[T])(h: PartialFunction[Throwable, Task[T]]): Task[T] =
      rt.recoverWith(h)
  }
}
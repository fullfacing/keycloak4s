package com.fullfacin.keycloak4s

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.implicits._
import com.fullfacing.keycloak4s.client.{Keycloak, KeycloakClient, KeycloakConfig}
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import org.json4s.Formats

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

object Main extends App {

  implicit val formats: Formats = org.json4s.DefaultFormats
  implicit val sttpBackend: AkkaHttpBackendL = new AkkaHttpBackendL(AkkaHttpBackend())

  val config = KeycloakConfig(authn = KeycloakConfig.Auth("master", "admin-cli", "fedb554a-f1f6-4b9e-ace8-2e0e5842ceef"))


  implicit val client: KeycloakClient[Task, Source[ByteString, Any]] =
    new KeycloakClient[Task, Source[ByteString, Any]](config)

  val clients = Keycloak.Users[Task, Source[ByteString, Any]]
  import scala.concurrent.duration._
  global.scheduleAtFixedRate(0 seconds, 1 second) {
    clients.getUsers("lessondesk").onErrorHandle(_.printStackTrace()).runToFuture
  }



  Console.readBoolean()
}

class AkkaHttpBackendL(delegate: SttpBackend[Future, Source[ByteString, Any]]) extends SttpBackend[Task, Source[ByteString, Any]] {
  override def send[T](request: Request[T, Source[ByteString, Any]]): Task[Response[T]] =
    Task.fromFuture(delegate.send(request))

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
package com.fullfacing.keycloak4s

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.implicits._
import com.fullfacing.keycloak4s.client.{Keycloak, KeycloakClient, KeycloakConfig}
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.json4s.Formats

import scala.concurrent.Future
import scala.language.postfixOps

object Main extends App {

  implicit val formats: Formats = org.json4s.DefaultFormats
  implicit val sttpBackend: AkkaHttpBackendL = new AkkaHttpBackendL(AkkaHttpBackend())

  val config = KeycloakConfig("http", "localhost", 8080, "demo", KeycloakConfig.Auth("master", "admin-cli", "6808820a-b662-4480-b832-f2d024eb6e03"))



  implicit val client: KeycloakClient[Task, Source[ByteString, Any]] =
    new KeycloakClient[Task, Source[ByteString, Any]](config)

  val clients = Keycloak.Roles[Task, Source[ByteString, Any]]
  import scala.concurrent.duration._
  global.scheduleOnce(0 seconds) {
    clients.getRolePermissions("4a89a463-4156-459f-85b4-bf85a5dcbf07").foreachL(println).onErrorHandle(_.printStackTrace()).runToFuture
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
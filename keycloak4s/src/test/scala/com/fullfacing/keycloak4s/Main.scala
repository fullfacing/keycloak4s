package com.fullfacing.keycloak4s

import java.util.UUID

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.fullfacing.keycloak4s.client.serialization.JsonFormats.default
import cats.implicits._
import com.fullfacing.keycloak4s.client.{Keycloak, KeycloakClient, KeycloakConfig}
import com.fullfacing.keycloak4s.models.RealmRepresentation
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future
import scala.language.postfixOps
import org.json4s.jackson.Serialization.writePretty

object Main extends App {

  implicit val sttpBackend: AkkaHttpBackendL = new AkkaHttpBackendL(AkkaHttpBackend())

  val config = KeycloakConfig("http", "localhost", 8080, "TestRealm", KeycloakConfig.Auth("master", "admin-cli", "7856293e-307c-4834-8b39-27d60874ebfb"))

  implicit val client: KeycloakClient[Task, Source[ByteString, Any]] =
    new KeycloakClient[Task, Source[ByteString, Any]](config)

  val clients = Keycloak.RealmsAdmin[Task, Source[ByteString, Any]]
  import scala.concurrent.duration._
  global.scheduleAtFixedRate(0 seconds, 60 seconds) {
    clients.getTopLevelRepresentation().foreachL(s => println(writePretty(s))).onErrorHandle(_.printStackTrace()).runToFuture
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
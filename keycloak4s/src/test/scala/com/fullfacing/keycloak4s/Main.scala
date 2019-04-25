package com.fullfacing.keycloak4s

import java.util.UUID

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.implicits._
import com.fullfacing.keycloak4s.client.serialization.JsonFormats._
import com.fullfacing.keycloak4s.client.{Keycloak, KeycloakClient, KeycloakConfig}
import com.fullfacing.keycloak4s.models.{ManagementPermission, Role}
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.json4s.jackson.Serialization._

import scala.concurrent.Future
import scala.language.postfixOps

object Main extends App {

  implicit val sttpBackend: AkkaHttpBackendL = new AkkaHttpBackendL(AkkaHttpBackend())
  val config = KeycloakConfig("http", "localhost", 8080, "master", KeycloakConfig.Auth("master", "admin-cli", "395e93aa-fb15-4477-a46c-62e6e2114c69"))
  implicit val client: KeycloakClient[Task, Source[ByteString, Any]] = new KeycloakClient[Task, Source[ByteString, Any]](config)

  val clients = Keycloak.Roles[Task, Source[ByteString, Any]]("master")

  val ids = List(
    UUID.fromString("2b104a13-1c62-4c05-905c-8b3b20f695d0"),
    UUID.fromString("65169b64-d469-4929-9c59-9014b13cfb56"),
    UUID.fromString("68baab53-a396-4dd3-94a4-7579424c212d"),
    UUID.fromString("dde5d460-d4e8-42a1-9d2a-6048fc971a2a")
  )

  val ids2 = List(
    UUID.fromString("2b104a13-1c62-4c05-905c-8b3b20f695d0"),
    UUID.fromString("9eb80c83-5927-4e64-b47d-db52eeb034d3")
  )
  clients.fetchCompositesAppLevelRoles(UUID.fromString("8c60089b-9065-40e2-b7ea-e6a5e57cef5b"), "NewRole", "account").foreachL(s => println(writePretty(s))).onErrorHandle(_.printStackTrace()).runToFuture
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
package com.fullfacing.keycloak4s.monix

import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.keycloak4s.client.KeycloakConfig
import com.fullfacing.keycloak4s.models.User
import com.fullfacing.keycloak4s.monix.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.monix.utilities.ObservableUtils
import com.fullfacing.keycloak4s.monix.utilities.ObservableUtils._
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.writePretty

import scala.io.StdIn
import scala.language.postfixOps

object MainMonix extends App {

  implicit val formats: Formats = org.json4s.DefaultFormats
  implicit val sttpBackend: MonixHttpBackendL = new MonixHttpBackendL(AsyncHttpClientMonixBackend())

  val config: KeycloakConfig = KeycloakConfig("http", "localhost", 8080, "demo", KeycloakConfig.Auth("master", "admin-cli", "6808820a-b662-4480-b832-f2d024eb6e03"))


  implicit val client: KeycloakClient = new KeycloakClient(config)

  /*{
    import com.softwaremill.sttp._
    import com.softwaremill.sttp.json4s.asJson
    implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

    val certEndpoint =
      uri"http://${config.host}:${config.port}/auth/realms/${config.authn.realm}/protocol/openid-connect/certs"

    val password = Map(
      "grant_type" -> "client_credentials",
      "client_id" -> config.authn.clientId,
      "client_secret" -> config.authn.clientSecret
    )

    def getCert(): Task[Map[String, String]] = {
      val a = sttp.get(certEndpoint)
        .body(password)
        .response(asJson[Map[String, String]])
        .send()

      a.map(_.body.fold(_ => Map.empty[String, String], a => a))
    }

//    getCert().foreach(_.foreach(println))
  }*/

  val clients = Keycloak.Keys
  clients.getRealmKeys().foreachL(s => println(writePretty(s))).onErrorHandle(_.printStackTrace()).runToFuture

  obs.walk[State, Seq[User]](State.Init)(ObservableUtils.fetchResources(i => Keycloak.Users.getUsers(first = Some(i)))).toListL.foreach(r => println(writePretty(r.flatten)))

  StdIn.readLine()
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
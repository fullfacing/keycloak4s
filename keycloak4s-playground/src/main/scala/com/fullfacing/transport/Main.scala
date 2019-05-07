package com.fullfacing.transport

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.ExitCode
import com.fullfacing.keycloak4s.admin.client.{Keycloak, KeycloakClient}
import com.fullfacing.keycloak4s.admin.serialization.JsonFormats.default
import com.fullfacing.keycloak4s.core.models.KeycloakConfig
import com.fullfacing.transport.backends.{AkkaHttpBackendL, MonixHttpBackendL}
import com.fullfacing.transport.handles.Akka
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import monix.eval.{Task, TaskApp}
import org.json4s.jackson.Serialization.writePretty

object Main extends TaskApp {
  def run(args: List[String]): Task[ExitCode] = Akka.connect().flatMap { _ =>

    /**
     * Config details of the Keycloak server to allow for Admin API calls.
     * Replace with applicable details.
     */
    val host: String        = "localhost"
    val port: Int           = 8088
    val targetRealm: String = "test" //Name of a Realm whose data will be accessed/manipulated.
    val adminRealm: String  = "master" //Name of a Realm with admin rights that can access/manipulate targetRealm.
    val adminClient: String = "admin-cli" //Name of the admin Client inside adminRealm.
    val adminSecret: String = "59241377-377a-4642-bae0-d19fdcc1ce5d" //Secret of adminClient.

    val authConfig  = KeycloakConfig.Auth(adminRealm, adminClient, adminSecret)
    val config      = KeycloakConfig("http", host, port, targetRealm, authConfig)

    /* KeycloakClient for Admin API calls. Uses any valid SttpBackend and can be modified for any Cats-Effect Concurrent-compatible monad. **/
    val genericClient: KeycloakClient[Task, Source[ByteString, Any]] = {
      implicit val backend: AkkaHttpBackendL = new AkkaHttpBackendL(AkkaHttpBackend())
      new KeycloakClient[Task, Source[ByteString, Any]](config)
    }

    /* Monix-specific alternative for Admin API calls. Includes additional Observable functionality. **/
    lazy val monixClient: KeycloakClient[Task, Source[ByteString, Any]] = {
      implicit val backend: MonixHttpBackendL = new MonixHttpBackendL(AsyncHttpClientMonixBackend())
      ??? //Client to be added after Monix submodule refactoring.
    }

    implicit val client: KeycloakClient[Task, Source[ByteString, Any]] = genericClient //slot in preferred client

    /* Example Usage: Provides access to the Users calls using the implicit client. **/
    val users = Keycloak.Users[Task, Source[ByteString, Any]]

    /* Example Usage: Returns and prints all Users for the given Realm. **/
    users.fetch().foreachL {
      case Left(l)  => println(l)
      case Right(r) => println(writePretty(r))
    }.map(_ => ExitCode.Success)
  }.onErrorHandle { ex => ex.printStackTrace(); ExitCode.Error }
}

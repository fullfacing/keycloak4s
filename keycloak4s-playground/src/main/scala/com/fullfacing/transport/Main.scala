package com.fullfacing.transport

import akka.util.ByteString
import cats.effect.ExitCode
import com.fullfacing.akka.monix.task.backend.AkkaMonixHttpBackend
import com.fullfacing.keycloak4s.admin.monix.client.{Keycloak => KeycloakM, KeycloakClient => KeycloakClientM}
import com.fullfacing.keycloak4s.core.models.{ConfigWithAuth, KeycloakConfig}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.fullfacing.transport.handles.Akka
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.json4s.jackson.Serialization.writePretty
import sttp.client3.SttpBackend

object Main extends TaskApp {
  
  /**
   * Config details of the Keycloak server to allow for Admin API calls.
   * Replace with applicable details.
   */
  lazy val config: ConfigWithAuth = {
    val host: String        = "localhost"
    val port: Int           = 8080
    val targetRealm: String = "master" //Name of a Realm whose data will be accessed/manipulated.
    val adminRealm: String  = "master" //Name of a Realm with admin rights that can access/manipulate targetRealm.
    val adminClient: String = "admin-cli" //Name of the admin Client inside adminRealm.
    val adminSecret: String = "???" //Secret of adminClient.

    val authConfig  = KeycloakConfig.Secret(adminRealm, adminClient, adminSecret)

    ConfigWithAuth("http", host, port, targetRealm, authConfig)
  }

  def run(args: List[String]): Task[ExitCode] = Akka.connect().flatMap { _ =>
      JsonFormats.default
    /* KeycloakClient for Admin API calls. Uses any valid SttpBackend and can be modified for any Cats-Effect Concurrent-compatible monad. **/
//    val genericClient: KeycloakClient[Future] = {
//      implicit val backend = AkkaHttpBackend()
//      new KeycloakClient[Future](config)
//    }

    /* Monix-specific alternative for Admin API calls. Includes additional Observable functionality. **/
    lazy val monixClient: KeycloakClientM[ByteString] = {
      implicit val backend: SttpBackend[Task, Observable[ByteString]] = AkkaMonixHttpBackend()
      new KeycloakClientM[ByteString](config)
    }

    implicit val clientM: KeycloakClientM[ByteString] = monixClient

    /* Example Usage: Provides access to the Users calls using the implicit client. **/
    val users = KeycloakM.Users

    /* Example Usage: Returns and prints all Users for the given Realm. **/
    users.fetch().foreachL {
      case Left(l)  => println(l)
      case Right(r) => println(writePretty(r.map(_.username)))
    }.map(_ => ExitCode.Success)
  }.onErrorHandle { ex => ex.printStackTrace(); ExitCode.Error }
}

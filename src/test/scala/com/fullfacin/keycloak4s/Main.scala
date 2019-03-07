package com.fullfacin.keycloak4s

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.Effect
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend

import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import com.fullfacing.keycloak4s.client.KeycloakConfig

import scala.concurrent.Future

//object Main extends App {
//
//  implicit val sttpBackend: SttpBackend[Future, Source[ByteString, Any]] = AkkaHttpBackendL[Task]()
//
//  val config = KeycloakConfig()
//  implicit val client: KeycloakClient[Future, Source[ByteString, Any]] =
//    new KeycloakClient[Future, Source[ByteString, Any]](config)
//
//  val clients = Keycloak.Clients
//}
//
//class AkkaHttpBackendL[F[_]: Effect](delegate: SttpBackend[Future, Source[ByteString, Any]]) extends SttpBackend[F, Source[ByteString, Any]] {
//  import scala.util.{Failure, Success}
//
//  override def send[T](request: Request[T, Source[ByteString, Any]]): F[Response[T]] = Effect[F].async { cb =>
//    delegate.send(request).onComplete {
//      case Success(value) => cb(Right(value))
//      case Failure(error) => cb(Left(error))
//    }
//  }
//
//  override def close(): Unit = delegate.close()
//
//  override def responseMonad: MonadError[F] =
//}

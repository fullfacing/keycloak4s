package com.fullfacing.keycloak4s.admin.client

import java.util.UUID

import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.implicits.{Anything, BodyMagnet}
import com.fullfacing.keycloak4s.admin.handles.Logging
import com.fullfacing.keycloak4s.admin.handles.Logging.logLeft
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Id, RequestT, SttpBackend, Uri, sttp}
import org.json4s.jackson.Serialization.read

import scala.collection.immutable.Seq
import scala.reflect._
import scala.reflect.runtime.universe.{TypeTag, typeOf}
import scala.util.control.NonFatal

class KeycloakClient[F[+_] : Concurrent, -S](config: KeycloakConfig)(implicit client: SttpBackend[F, S]) extends TokenManager[F, S](config) {

  val realm: String = config.realm

  private implicit def ma[A : Anything]: Manifest[A] = implicitly[Anything[A]].manifest

  /* URI Builder **/
  private[client] def createUri(path: Seq[String], query: Seq[KeyValue]) = Uri(
    scheme         = config.scheme,
    userInfo       = None,
    host           = config.host,
    port           = Some(config.port),
    path           = Seq("auth", "admin", "realms") ++ path,
    queryFragments = query,
    fragment       = None
  )

  /* HTTP Call Builders **/

  private def setResponse[A <: Any : Manifest](request: RequestT[Id, String, Nothing])(implicit tag: TypeTag[A], cId: UUID)
  : F[Either[KeycloakSttpException, RequestT[Id, A, Nothing]]] = {

    val response = request.mapResponse { raw =>
      Logging.requestSuccessful(raw, cId)
      read[A](if (tag.tpe =:= typeOf[Unit]) "null" else raw)
    }

    withAuth(response)
  }

  private def call[B <: Any : Manifest](request: RequestT[Id, String, Nothing], requestInfo: RequestInfo): F[Either[KeycloakError, B]] = {
    implicit val cId: UUID = UUID.randomUUID()

    val resp = setResponse[B](request)

    Logging.requestSent(requestInfo, cId)

    val response = F.flatMap(resp) {
      case Right(r) => F.map(r.send())(liftM(_, requestInfo))
      case Left(e)  => F.unit(e.asLeft[B])
    }

    F.handleError[Either[KeycloakError, B]](response) {
      case NonFatal(ex) => F.unit(KeycloakThrowable(ex).asLeft[B])
    }.map(logLeft(_)(Logging.requestFailed(cId, _)))
  }

  /* REST Protocol Calls **/
  def get[A : Anything](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request = sttp.get(createUri(path, query))
    call[A](request, buildRequestInfo(path, "GET", ()))
  }

  def put[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = sttp.put(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "PUT", injected.body))
  }

  def post[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = sttp.post(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "POST", injected.body))
  }

  def delete[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = sttp.delete(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "DELETE", injected.body))
  }
}
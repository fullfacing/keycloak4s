package com.fullfacing.keycloak4s

import java.io.File
import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.apollo.core.networking.wire.serialization.{JsonFormats, JsonSerializer}
import com.fullfacing.apollo.core.protocol.ResponseCode
import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import monix.eval.Task
import monix.reactive.Observable
import org.json4s.Formats
import org.json4s.native.Serialization.{read, write}

import scala.collection.immutable.Seq

object SttpClient {
  /* Implicits **/

  implicit val formats: Formats = JsonFormats.default ++ EnumSerializers.all
  implicit val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientMonixBackend()

  /* Server Details **/

  private val scheme  = "http"
  private val host    = "localhost"    //TODO Add correct host
  private val port    = Some(8192)  //TODO Add correct port

  /* URI Builder **/

  private def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme          = scheme,
    userInfo        = None,
    host            = host,
    port            = port,
    path            = path,
    queryFragments  = queries,
    fragment        = None
  )

  /* STTP Client Error **/

  private val error = ErrorPayload(ResponseCode.InternalServerError, "Call to Keycloak server failed.")

  /* Type aliases **/

  type UnsetRequest   = Request[String, Nothing]
  type StringRequest  = RequestT[Id, String, Nothing]
  type ByteRequest    = RequestT[Id, Array[Byte], Nothing]

  /* STTP Client Function Components **/

  private def setJsonBody[A](body: A, req: UnsetRequest): StringRequest = req.contentType(ContentTypes.Json.value).body(write(body))

  private def setMultipartBody(mp: Multipart, req: UnsetRequest): StringRequest = req.multipartBody(mp)

  private def setJsonResponse(req: StringRequest): StringRequest = req.response(asString)

  private def setByteResponse(req: StringRequest): ByteRequest = req.response(asByteArray)

  private def deserializeJson[A](resp: Task[Response[String]])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] =
    resp.map(_.body.fold(_ => error.asLeft[A], read[A](_).asRight))

  private def deserializeBytes[A <: AnyRef](resp: Task[Response[Array[Byte]]])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] =
    resp.map(_.body.fold(_ => error.asLeft[A], JsonSerializer.fromBytes[A](_).asRight))

  private def sendRequestJson[A](implicit ma: Manifest[A]): StringRequest => Task[Either[ErrorPayload, A]] =
    (setJsonResponse _).andThen(_.send[Task]).andThen(deserializeJson[A])

  private def sendRequestBytes[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => Task[Either[ErrorPayload, A]] =
    (setByteResponse _).andThen(_.send[Task]).andThen(deserializeBytes[A])

  private def prepareRequest[A](req: A): UnsetRequest => StringRequest = req match {
    case mp: Multipart  => (setMultipartBody _).tupled(mp, _)
    case other          => (setJsonBody[A] _).tupled(other, _)
  }

  private def prepareResponse[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => Task[Either[ErrorPayload, A]] =
    if (ma <:< manifest[File]) sendRequestBytes[A] else sendRequestJson[A]

  /* REST Protocol Calls **/

  def delete[A](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    sendRequestJson[A](manifest)(sttp.delete(uri))
  }

  def delete[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.delete(uri))
  }

  def get[A](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    sendRequestJson[A](manifest)(sttp.get(uri))
  }

  def put[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.put(uri))
  }

  def put[A](path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    sendRequestJson[A](manifest)(sttp.put(uri))
  }

  def post[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.post(uri))
  }

  def post[A](path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    sendRequestJson[A](manifest)(sttp.post(uri))
  }

  def options[A](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    sendRequestJson[A](manifest)(sttp.options(uri))
  }
}

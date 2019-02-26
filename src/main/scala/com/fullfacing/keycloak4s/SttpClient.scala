package com.fullfacing.keycloak4s

import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.apollo.core.networking.wire.serialization.ContentType
import com.fullfacing.apollo.core.networking.wire.serialization.JsonFormats.default
import com.fullfacing.apollo.core.protocol.ResponseCode
import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import monix.eval.Task
import monix.reactive.Observable
import org.json4s.Extraction
import org.json4s.native.Serialization.read

import scala.collection.immutable.Seq

object SttpClient {
  implicit val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientMonixBackend()

  private val scheme  = "http"
  private val host    = ""    //TODO Add correct host
  private val port    = None  //TODO Add correct port

  private def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme          = scheme,
    userInfo        = None,
    host            = host,
    port            = port,
    path            = path,
    queryFragments  = queries,
    fragment        = None
  )

  private val error = ErrorPayload(ResponseCode.InternalServerError, "Call to Keycloak server failed.")

  private def makeCall[A](request: Request[String, Nothing])(implicit mb: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val task = request
      .response(asString)
      .send()

    task.map(_.body.fold(_ => error.asLeft[A], read[A](_).asRight))
  }

  private def makeCall[A, B](body: A, request: Request[String, Nothing])(implicit mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val bodyMap = Extraction
      .decompose(body)
      .extract[Map[String, String]]

    val task = request
      .contentType(ContentType.Json)
      .body(bodyMap)
      .response(asString)
      .send()

    task.map(_.body.fold(_ => error.asLeft[B], read[B](_).asRight))
  }

  def delete[A](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit mb: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    makeCall[A](sttp.delete(uri), queries)
  }

  def get[A](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit mb: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    makeCall[A](sttp.get(uri), queries)
  }

  def put[A, B](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    makeCall[A, B](body, sttp.put(uri), queries)
  }

  def post[A, B](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit mb: Manifest[B]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    makeCall[A, B](body, sttp.post(uri), queries)
  }

  /* Bodiless POST **/
  def post[A](path: Seq[String], queries: Seq[KeyValue])(implicit mb: Manifest[A]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    makeCall[A](sttp.post(uri), queries)
  }
}

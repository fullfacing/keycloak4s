package com.fullfacing.keycloak4s

import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.apollo.core.networking.wire.serialization.ContentType
import com.fullfacing.apollo.core.networking.wire.serialization.JsonFormats.default
import com.fullfacing.apollo.core.protocol.ResponseCode
import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
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
  private val host    = ???
  private val port    = ???

  private def createUri(path: Seq[String]) = Uri(
    scheme  = scheme,
    host    = host,
    port    = port,
    path    = path
  )

  private def createError(protocol: String) = ErrorPayload(ResponseCode.InternalServerError, s"$protocol to Keycloak server failed.")

  def delete(path: Seq[String]): Task[Either[ErrorPayload, Unit]] = {
    val uri = createUri(path)

    val task = sttp
      .delete(uri)
      .response(asString)
      .send()

    task.map(_.body.fold(_ => createError("DELETE").asLeft[Unit], _ => ().asRight))
  }

  def get[A](path: Seq[String]): Task[Either[ErrorPayload, A]] = {
    val uri = createUri(path)

    val task = sttp
      .get(uri)
      .response(asString)
      .send()

    task.map(_.body.fold(_ => createError("GET").asLeft[A], r => read[A](r).asRight))
  }

  def post[A, B](request: A, path: Seq[String]): Task[Either[ErrorPayload, B]] = {
    val uri = createUri(path)

    val body = Extraction
      .decompose(request)
      .extract[Map[String, String]]

    val task = sttp
      .post(uri)
      .contentType(ContentType.Json)
      .body(body)
      .response(asString)
      .send()

    task.map(_.body.fold(_ => createError("POST").asLeft[B], r => read[B](r).asRight))
  }
}

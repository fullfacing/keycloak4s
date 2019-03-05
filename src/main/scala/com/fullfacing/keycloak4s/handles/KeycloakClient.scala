package com.fullfacing.keycloak4s.handles

import java.io.File
import java.nio.charset.StandardCharsets

import cats.implicits._
import com.fullfacing.keycloak4s.EnumSerializers
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{Id, MonadError, Multipart, Request, RequestT, Response, SttpBackend, Uri, asByteArray, sttp}
import org.json4s.Formats
import org.json4s.native.Serialization

import scala.collection.immutable.Seq

class KeycloakClient[R[_], -M](config: KeycloakConfig)(implicit client: SttpBackend[R, M], F: MonadError[R]) {

  /* Implicits */
  implicit val formats: Formats = org.json4s.DefaultFormats ++ EnumSerializers.all
  implicit val serialization: Serialization.type = org.json4s.native.Serialization

  /* URI Builder **/
  private def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme          = config.scheme,
    userInfo        = None,
    host            = config.host,
    port            = Some(config.port),
    path            = Seq("auth", "admin", "realms") ++ path,
    queryFragments  = queries,
    fragment        = None
  )

  /* Type Aliases */
  type UnsetRequest   = Request[String, Nothing]
  type StringRequest  = RequestT[Id, String, Nothing]
  type ByteRequest    = RequestT[Id, Array[Byte], Nothing]

  private def setEncodedData(form: Map[String, String], req: UnsetRequest): StringRequest =
    req.contentType(ContentTypes.UrlEncoded.value).body(form)

  private def setJsonBody[A](body: A, req: UnsetRequest): StringRequest =
    req.contentType(ContentTypes.Json.value).body(Serialization.write(body))

  private def setMultipartBody(mp: Multipart, req: UnsetRequest): StringRequest =
    req.multipartBody(mp)

  private def setJsonResponse[A <: AnyRef: Manifest](req: StringRequest): StringRequest =
    req.response(asJson[A])

  private def setByteResponse(req: StringRequest): ByteRequest =
    req.response(asByteArray)

  private def setAuthHeader(token: String): StringRequest => StringRequest =
    req => req.header("Authorization", s"Bearer $token")

  private def sendRequestJson[A](implicit ma: Manifest[A]): StringRequest => R[A] =
    (setJsonResponse _).andThen(_.response(asJson[A]).send[R]).andThen(fromString[A])

  private def sendRequestBytes[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => R[A] =
    (setByteResponse _).andThen(_.response(asJson[A])).andThen(fromBytes[A])

  private def prepareRequest[A](req: A): UnsetRequest => StringRequest = req match {
    case m: Map[String, String] => (setEncodedData _).tupled(m, _)
    case mp: Multipart          => (setMultipartBody _).tupled(mp, _)
    case other                  => (setJsonBody[A] _).tupled(other, _)
  }

  private def prepareResponse[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => R[A] =
    if (ma <:< manifest[File]) sendRequestBytes[A] else sendRequestJson[A]

  /* REST Protocol Calls **/
  def delete[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A]): R[A] = {
    val uri = createUri(path, queries)
    prepareResponse[A](manifest)(sttp.delete(uri))
  }

  def delete[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue]): R[B] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.delete(uri))
  }

  def get[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    prepareResponse[A](manifest)(sttp.get(uri))
  }

  def put[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[B] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.put(uri))
  }

  def put[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    prepareResponse[A](manifest)(sttp.put(uri))
  }

  def post[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[B] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(prepareResponse[B])(sttp.post(uri))
  }

  def post[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    prepareResponse[A](manifest)(sttp.post(uri))
  }
}

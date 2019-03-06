package com.fullfacing.keycloak4s.handles

import java.nio.charset.StandardCharsets

import com.fullfacing.keycloak4s.EnumSerializers
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{Id, MonadError, Multipart, Request, RequestT, Response, SttpBackend, Uri, asByteArray, sttp}
import org.json4s.Formats
import org.json4s.native.Serialization

import scala.collection.immutable.Seq

class KeycloakClient[R[_], -S](config: KeycloakConfig)(implicit client: SttpBackend[R, S], F: MonadError[R]) {

  /* Implicits */
  implicit val formats: Formats = org.json4s.DefaultFormats ++ EnumSerializers.all
  implicit val serialization: Serialization.type = org.json4s.native.Serialization

  /* URI Builder **/
  private def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme         = config.scheme,
    userInfo       = None,
    host           = config.host,
    port           = Some(config.port),
    path           = Seq("auth", "admin", "realms") ++ path,
    queryFragments = queries,
    fragment       = None
  )

  /* Type Aliases */
  type UnsetRequest  = Request[String, Nothing]
  type StringRequest = RequestT[Id, String, Nothing]
  type ByteRequest   = RequestT[Id, Array[Byte], Nothing]

  private def sendRequestJson[A <: AnyRef: Manifest](req: StringRequest): R[Response[A]] = {
    req.response(asJson[A]).send()
  }

  private def sendRequestBytes[A <: AnyRef: Manifest](req: StringRequest): R[Response[A]] = {
    req.response(asByteArray)
       .mapResponse(bytes => new String(bytes, StandardCharsets.UTF_8))
       .mapResponse(Serialization.read[A])
       .send()
  }

  private def preparePayload[A](req: Request[String, Nothing], data: A): StringRequest = data match {
    case payload: Map[_, _] =>
      req.contentType(ContentTypes.UrlEncoded).body(payload)
    case payload: Multipart =>
      req.contentType(ContentTypes.Multipart).multipartBody(payload)
    case payload =>
      req.contentType(ContentTypes.Json).body(payload)
  }

  /* REST Protocol Calls **/
  def get[A <: AnyRef: Manifest](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    preparePayload(sttp.get(uri), )
    F.flatMap(prepareResponse[A](sttp.get(uri)))(_.body match {
      case Left(ex) => F.error[A](new Throwable(ex))
      case Right(r) => F.unit(r)
    })
  }

  // ------------------------------------------------------------- //
  // ---------------------------- PUT ---------------------------- //
  // ------------------------------------------------------------- //
  def put[A: Manifest, B <: AnyRef: Manifest](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[B] = {
    val uri = createUri(path, queries)
    preparePayload[A](body).andThen(prepareResponse[B])(sttp.put(uri))
  }

  def put[A <: AnyRef: Manifest](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    prepareResponse[A](sttp.put(uri))
  }


  // -------------------------------------------------------------- //
  // ---------------------------- POST ---------------------------- //
  // -------------------------------------------------------------- //
  def post[A <: AnyRef: Manifest](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    sttp.post(uri).response(asJson[A])
  }

  def post[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])
                                    (implicit ma: Manifest[A], mb: Manifest[B]): R[B] = {
    val uri = createUri(path, queries)
    sttp.post(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]).send()
  }

  def post[A <: AnyRef](payload: Map[String, String], path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])
                       (implicit ma: Manifest[A]): R[A] = {
    val uri = createUri(path, queries)
    sttp.post(uri).contentType(ContentTypes.UrlEncoded).body(payload).response(asJson[A]).send()
  }

  def post[A <: AnyRef](payload: Multipart, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])
                       (implicit ma: Manifest[A]): R[A] = {
    val uri = createUri(path, queries)
    sttp.post(uri).contentType(ContentTypes.Multipart).body(payload).response(asJson[A]).send()
  }

  // ---------------------------------------------------------------- //
  // ---------------------------- DELETE ---------------------------- //
  // ---------------------------------------------------------------- //
  def delete[A <: AnyRef: Manifest](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue]): R[Unit] = {
    val uri = createUri(path, queries)
    prepareResponse[A](manifest)(sttp.delete(uri))
  }

  def delete[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue]): R[B] = {
    val uri = createUri(path, queries)
    preparePayload[A](body).andThen(prepareResponse[B])(sttp.delete(uri))
  }
}
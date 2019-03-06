package com.fullfacing.keycloak4s.handles

import cats.data.Kleisli
import com.fullfacing.keycloak4s.EnumSerializers
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{MonadError, Multipart, SttpBackend, Uri, sttp}
import org.json4s.Formats
import org.json4s.native.Serialization

import scala.collection.immutable.Seq

class KeycloakClient[R[_], -S](config: KeycloakConfig)(implicit client: SttpBackend[R, S], F: MonadError[R]) {

  /* Implicits */
  implicit val formats: Formats = org.json4s.DefaultFormats ++ EnumSerializers.all
  implicit val serialization: Serialization.type = org.json4s.native.Serialization

  /* URI Builder **/
  private def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme = config.scheme,
    userInfo = None,
    host = config.host,
    port = Some(config.port),
    path = Seq("auth", "admin", "realms") ++ path,
    queryFragments = queries,
    fragment = None
  )

  private def liftM[A](response: Either[String, A]): R[A] = response match {
    case Left(err) => F.error(new Throwable(err))
    case Right(rsp) => F.unit(rsp)
  }

  /* REST Protocol Calls **/
  def get[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    val response = sttp.get(uri).response(asJson[A]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  // ------------------------------------------------------------- //
  // ---------------------------- PUT ---------------------------- //
  // ------------------------------------------------------------- //
  def put[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                   (implicit mb: Manifest[B]): R[B] = {
    val uri = createUri(path, queries)
    val response = sttp.put(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  def put[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    val response = sttp.put(uri).response(asJson[A]).send()

    F.flatMap(response)(r => liftM(r.body))
  }


  // -------------------------------------------------------------- //
  // ---------------------------- POST ---------------------------- //
  // -------------------------------------------------------------- //
  def post[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): R[A] = {
    val uri = createUri(path, queries)
    val response = sttp.post(uri).response(asJson[A]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  def post[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                    (implicit mb: Manifest[B]): R[B] = {
    val uri = createUri(path, queries)
    val response = sttp.post(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  def post[A <: AnyRef](payload: Map[String, String], path: Seq[String], queries: Seq[KeyValue])
                       (implicit ma: Manifest[A]): R[A] = {
    val uri = createUri(path, queries)
    val response = sttp.post(uri).contentType(ContentTypes.UrlEncoded).body(payload).response(asJson[A]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  def post[A <: AnyRef](payload: Multipart, path: Seq[String], queries: Seq[KeyValue])
                       (implicit ma: Manifest[A]): R[A] = {
    val uri = createUri(path, queries)
    val response = sttp.post(uri).contentType(ContentTypes.Multipart).body(payload).response(asJson[A]).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  // ---------------------------------------------------------------- //
  // ---------------------------- DELETE ---------------------------- //
  // ---------------------------------------------------------------- //
  def delete[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): R[Unit] = {
    val uri = createUri(path, queries)
    val response = sttp.delete(uri).mapResponse(_ => ()).send()

    F.flatMap(response)(r => liftM(r.body))
  }

  def delete[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                      (implicit mb: Manifest[B]): R[B] = {
    val uri = createUri(path, queries)
    val response = sttp.delete(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]).send()

    F.flatMap(response)(r => liftM(r.body))
  }
}


object KeycloakClient {
  type Request[R[_], S, A] = Kleisli[R, KeycloakClient[R, S], A]
}
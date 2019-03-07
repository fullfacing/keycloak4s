package com.fullfacing.keycloak4s.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{MonadError, Multipart, SttpBackend, Uri, sttp}
import org.json4s.Formats

import scala.collection.immutable.Seq

class KeycloakClient[F[_] : Concurrent, -S](config: KeycloakConfig)(implicit client: SttpBackend[F, S], formats: Formats) extends TokenManager[F, S](config) {

  private val F: MonadError[F] = client.responseMonad

  /* URI Builder **/
  private[client] def createUri(path: Seq[String], queries: Seq[KeyValue]) = Uri(
    scheme = config.scheme,
    userInfo = None,
    host = config.host,
    port = Some(config.port),
    path = Seq("auth", "admin", "realms") ++ path,
    queryFragments = queries,
    fragment = None
  )

  private def liftM[A](response: Either[String, A]): F[A] = response match {
    case Left(err) => F.error(new Throwable(err))
    case Right(rsp) => F.unit(rsp)
  }


  /* REST Protocol Calls **/
  def get[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): F[A] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.get(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def get[A <: AnyRef : Manifest](path: Seq[String]): F[A] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.get(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  // ------------------------------------------------------------- //
  // ---------------------------- PUT ---------------------------- //
  // ------------------------------------------------------------- //

  def putNoContent[A <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.put(uri).contentType(ContentTypes.Json).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def put(path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.put(uri).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def put[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                   (implicit mb: Manifest[B]): F[B] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.put(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def put[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): F[A] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.put(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  // -------------------------------------------------------------- //
  // ---------------------------- POST ---------------------------- //
  // -------------------------------------------------------------- //

  def postNoContent(path: Seq[String]): F[Unit] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def postNoContent[A <: AnyRef](payload: A, path: Seq[String]): F[Unit] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.Json).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def postNoContent[A <: AnyRef](payload: Map[String, String], path: Seq[String]): F[Unit] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.UrlEncoded).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }


  def post[A <: AnyRef](path: Seq[String])(implicit mb: Manifest[A]): F[A] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }


  def post[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String])
                                    (implicit mb: Manifest[B]): F[B] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def post[A <: AnyRef](payload: Map[String, String], path: Seq[String])
                       (implicit ma: Manifest[A]): F[A] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.UrlEncoded).body(payload).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def post[A <: AnyRef](payload: Multipart, path: Seq[String])
                       (implicit ma: Manifest[A]): F[A] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.Multipart).body(payload).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  // ---------------------------------------------------------------- //
  // ---------------------------- DELETE ---------------------------- //
  // ---------------------------------------------------------------- //
  def delete[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def delete[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                      (implicit mb: Manifest[B]): F[B] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def deleteNoContent[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }
}
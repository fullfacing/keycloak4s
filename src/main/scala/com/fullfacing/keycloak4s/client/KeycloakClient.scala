package com.fullfacing.keycloak4s.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{Multipart, SttpBackend, Uri, sttp}
import org.json4s.Formats

import scala.collection.immutable.Seq

class KeycloakClient[F[_] : Concurrent, -S](config: KeycloakConfig)(implicit client: SttpBackend[F, S], formats: Formats) extends TokenManager[F, S](config) {

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


  private def determineContent[A <: AnyRef](payload: A): String = payload match {
    case _: Map[_, _] => ContentTypes.UrlEncoded
    case _: Multipart => ContentTypes.Multipart
    case _            => ContentTypes.Json
  }

  /* REST Protocol Calls **/
  def get[A <: AnyRef : Manifest](path: Seq[String], queries: Seq[KeyValue]): F[A] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.get(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def get[A <: AnyRef : Manifest](path: Seq[String]): F[A] =
    get[A](path, Seq.empty[KeyValue])

  // ------------------------------------------------------------- //
  // ---------------------------- PUT ---------------------------- //
  // ------------------------------------------------------------- //

  def put[A <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
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

  /* Without Query **/
  def put[A <: AnyRef](payload: A, path: Seq[String]): F[Unit] =
    put[A](payload, path, Seq.empty[KeyValue])

  def put(path: Seq[String]): F[Unit] =
    put(path, Seq.empty[KeyValue])

  def put[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String])(implicit mb: Manifest[B]): F[B] =
    put[A, B](payload, path, Seq.empty[KeyValue])

  def put[A <: AnyRef](path: Seq[String])(implicit mb: Manifest[A]): F[A] =
    put[A](path, Seq.empty[KeyValue])

  // -------------------------------------------------------------- //
  // ---------------------------- POST ---------------------------- //
  // -------------------------------------------------------------- //

  def post(path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def post[A <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val contentType = determineContent(payload)
    val response = withAuth(sttp.post(uri).contentType(contentType).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def post[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue])(implicit mb: Manifest[A]): F[A] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.post(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def post[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                    (implicit mb: Manifest[B]): F[B] = {
    val uri = createUri(path, Seq.empty[KeyValue])
    val response = withAuth(sttp.post(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  /* Without Query **/
  def post(path: Seq[String]): F[Unit] =
    post(path, Seq.empty[KeyValue])

  def post[A <: AnyRef](payload: A, path: Seq[String]): F[Unit] =
    post[A](payload, path, Seq.empty[KeyValue])

  def post[A <: AnyRef](path: Seq[String])(implicit mb: Manifest[A]): F[A] =
    post[A](path, Seq.empty[KeyValue])

  def post[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String])(implicit mb: Manifest[B]): F[B] =
    post[A, B](payload, path, Seq.empty[KeyValue])


  // ---------------------------------------------------------------- //
  // ---------------------------- DELETE ---------------------------- //
  // ---------------------------------------------------------------- //
  def delete[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue])
                                    (implicit mb: Manifest[A]): F[A] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).response(asJson[A]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def delete[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue])
                                      (implicit mb: Manifest[B]): F[B] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).contentType(ContentTypes.Json).body(payload).response(asJson[B]))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def delete(path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  def delete[A <: AnyRef](payload: A, path: Seq[String], queries: Seq[KeyValue]): F[Unit] = {
    val uri = createUri(path, queries)
    val response = withAuth(sttp.delete(uri).body(payload).mapResponse(_ => ()))

    F.flatMap(response)(r => F.flatMap(r.send())(rr => liftM(rr.body)))
  }

  /* Without Query **/
  def delete[A <: AnyRef](path: Seq[String])(implicit mb: Manifest[A]): F[A] =
    delete[A](path, Seq.empty[KeyValue])

  def delete[A <: AnyRef, B <: AnyRef](payload: A, path: Seq[String])(implicit mb: Manifest[B]): F[B] =
    delete[A, B](payload, path, Seq.empty[KeyValue])

  def delete(path: Seq[String]): F[Unit] =
    delete(path, Seq.empty[KeyValue])

  def delete[A <: AnyRef](payload: A, path: Seq[String]): F[Unit] =
    delete[A](payload, path, Seq.empty[KeyValue])
}
package com.fullfacing.keycloak4s.handles

import java.io.File

import com.fullfacing.keycloak4s.EnumSerializers
import com.fullfacing.keycloak4s.handles.Logging.logger
import com.fullfacing.keycloak4s.models.enums.ContentTypes
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Id, MonadAsyncError, Multipart, Request, RequestT, Response, SttpBackend, Uri, asByteArray, asString, sttp}
import org.json4s.Formats

import scala.collection.immutable.Seq

class KeycloakClient[R[_], -M](config: KeycloakConfig)(implicit client: SttpBackend[R, M], F: MonadAsyncError[R]) {

  /* Implicits */
  implicit val formats: Formats = org.json4s.DefaultFormats ++ EnumSerializers.all

  /* Server Details */
  private val scheme   = "http"
  private val host     = "localhost"  //TODO Add correct host
  private val port     = Some(8080)   //TODO Add correct port
  private val basePath = Seq("auth", "admin", "realms")

  /* URI Builder **/
  private def createUri(path: Seq[String], queries: Seq[KeyValue], bPath: Seq[String] = basePath) = Uri(
    scheme          = scheme,
    userInfo        = None,
    host            = host,
    port            = port,
    path            = bPath ++ path,
    queryFragments  = queries,
    fragment        = None
  )

  /* STTP Client Error Handling **/
  private def handleError[A](err: String): Either[ErrorPayload, A] = {
    logger.error(s"${Console.RED}Error Response: $err${Console.RESET}")
    ErrorPayload(ResponseCode.InternalServerError, "Call to Keycloak server failed.").asLeft[A]
  }

  /* Type Aliases */
  type UnsetRequest   = Request[String, Nothing]
  type StringRequest  = RequestT[Id, String, Nothing]
  type ByteRequest    = RequestT[Id, Array[Byte], Nothing]
  
  type ErrorPayload = String

  /**
    *
    * @param form
    * @param req
    * @return
    */
  private def setEncodedData(form: Map[String, String], req: UnsetRequest): StringRequest =
    req.contentType(ContentTypes.UrlEncoded.value).body(form)

  private def setJsonBody[A](body: A, req: UnsetRequest): StringRequest =
    req.contentType(ContentTypes.Json.value).body(Serializ.(body))

  private def setMultipartBody(mp: Multipart, req: UnsetRequest): StringRequest =
    req.multipartBody(mp)

  private def setJsonResponse(req: StringRequest): StringRequest =
    req.response(asString)

  private def setByteResponse(req: StringRequest): ByteRequest =
    req.response(asByteArray)

  private def setAuthHeader(token: String): StringRequest => StringRequest =
    req => req.header("Authorization", s"Bearer $token")

  private def deserializeJson[A](resp: R[Response[String]])(implicit ma: Manifest[A]): R[Either[ErrorPayload, A]] =
    F.map(resp)(_.body.fold(handleError, read[A](_).asRight))

//  private def deserializeBytes[A <: AnyRef](resp: F[Response[Array[Byte]]])(implicit ma: Manifest[A]): F[Either[ErrorPayload, A]] =
//    resp.map(_.body.fold(handleError, JsonSerializer.fromBytes[A](_).asRight))
//
//  private def sendRequestJson[A](implicit ma: Manifest[A]): StringRequest => F[Either[ErrorPayload, A]] =
//    (setJsonResponse _).andThen(_.send[Task]).andThen(deserializeJson[A])
//
//  private def sendRequestBytes[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => F[Either[ErrorPayload, A]] =
//    (setByteResponse _).andThen(_.send[Task]).andThen(deserializeBytes[A])
//
  private def prepareRequest[A](req: A): UnsetRequest => StringRequest = req match {
    case m: Map[String, String] => (setEncodedData _).tupled(m, _)
    case mp: Multipart          => (setMultipartBody _).tupled(mp, _)
    case other                  => (setJsonBody[A] _).tupled(other, _)
  }

  private def prepareResponse[A <: AnyRef](implicit ma: Manifest[A]): StringRequest => F[Either[ErrorPayload, A]] =
    if (ma <:< manifest[File]) sendRequestBytes[A] else sendRequestJson[A]

  /* REST Protocol Calls **/

  def delete[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], authToken: String): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    setAuthHeader(authToken).andThen(prepareResponse[A](manifest))(sttp.delete(uri))
  }

  def delete[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B], authToken: String): F[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(setAuthHeader(authToken)).andThen(prepareResponse[B])(sttp.delete(uri))
  }

  def get[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], authToken: String): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    setAuthHeader(authToken).andThen(prepareResponse[A](manifest))(sttp.get(uri))
  }

  def put[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B], authToken: String): F[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(setAuthHeader(authToken)).andThen(prepareResponse[B])(sttp.put(uri))
  }

  def put[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A], authToken: String): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    setAuthHeader(authToken).andThen(prepareResponse[A](manifest))(sttp.put(uri))
  }

  def post[A, B <: AnyRef](body: A, path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], mb: Manifest[B], authToken: String): F[Either[ErrorPayload, B]] = {
    val uri = createUri(path, queries)
    prepareRequest[A](body).andThen(setAuthHeader(authToken)).andThen(prepareResponse[B])(sttp.post(uri))
  }

  def post[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue])(implicit ma: Manifest[A], authToken: String): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    setAuthHeader(authToken).andThen(prepareResponse[A](manifest))(sttp.post(uri))
  }

  def options[A <: AnyRef](path: Seq[String], queries: Seq[KeyValue] = Seq.empty[KeyValue])(implicit ma: Manifest[A], authToken: String): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, queries)
    setAuthHeader(authToken).andThen(prepareResponse[A](manifest))(sttp.options(uri))
  }

  def auth[A <: AnyRef](form: Map[String, String], path: Seq[String])(implicit ma: Manifest[A]): F[Either[ErrorPayload, A]] = {
    val uri = createUri(path, Seq.empty[KeyValue], Seq("auth", "realms"))
    prepareRequest(form).andThen(prepareResponse[A](manifest))(sttp.post(uri))
  }
}

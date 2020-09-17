package com.fullfacing.keycloak4s.admin.monix.bio.client

import java.util.UUID

import com.fullfacing.keycloak4s.admin.client.implicits.{Anything, BodyMagnet}
import com.fullfacing.keycloak4s.admin.handles.Logging
import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization.read
import sttp.client.{Identity, NothingT, RequestT, Response, SttpBackend, asString, basicRequest}
import sttp.model.Uri.QuerySegment.KeyValue
import sttp.model.{StatusCode, Uri}

import scala.collection.immutable.Seq
import scala.reflect.Manifest
import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

class KeycloakClient[-S](config: ConfigWithAuth)(implicit client: SttpBackend[IO[Throwable, *], S, NothingT]) extends TokenManager[S](config) {

  val realm: String = config.realm

  /* Extracts the Manifest out of an implicit Anything. **/
  private implicit def ma[A : Anything]: Manifest[A] = implicitly[Anything[A]].manifest

  /* URI Builder **/
  private[client] def createUri(path: Seq[String], query: Seq[KeyValue]) = Uri.apply(
    scheme         = config.scheme,
    userInfo       = None,
    host           = config.host,
    port           = Some(config.port),
    path           = Seq("auth", "admin", "realms") ++ path,
    querySegments  = query,
    fragment       = None
  )

  /* HTTP Call Builders **/

  private def setResponse[A <: Any : Manifest](request: RequestT[Identity, Either[String, String], Nothing])
                                              (implicit tag: TypeTag[A], cId: UUID)
  : RequestT[Identity, Either[String, A], Nothing] = {

    val respAs = asString.mapWithMetadata { case (raw, meta) =>
      raw.map { body =>
        Logging.requestSuccessful(body, cId)

        if (tag.tpe =:= typeOf[Unit]) {println("1");read[A]("null")}
        else if (tag.tpe =:= typeOf[Headers]) {println("2");meta.headers.map(h => h.name -> h.value).toMap.asInstanceOf[A]}
        else {println("3");read[A](body)}
      }
    }

    request.response(respAs)
  }

  private def call[B <: Any : Manifest](request: RequestT[Identity, Either[String, String], Nothing], requestInfo: RequestInfo): IO[KeycloakError, B] = {
    implicit val cId: UUID = UUID.randomUUID()

    val resp = setResponse[B](request.header("Accept", "application/json"))

    def sendWithLogging(req: RequestT[Identity, Either[String, B], Nothing]): IO[Throwable, Response[Either[String, B]]] = {
      UIO(Logging.requestSent(requestInfo, cId)).flatMap(_ => req.send())
    }

    def retryWithLogging(req: RequestT[Identity, Either[String, B], Nothing]): IO[Throwable, Response[Either[String, B]]] = {
      UIO(Logging.retryUnauthorized(requestInfo, cId)).flatMap(_ => req.send())
    }

    val response = withAuth(resp).flatMap { r =>
      sendWithLogging(r)
        .flatMap(liftM(_, requestInfo))
        .mapError {
          case KeycloakSttpException(StatusCode.Unauthorized.code, _, _, _, _) =>
            withAuthNewToken(resp).flatMap(r => retryWithLogging(r).map(liftM(_, requestInfo)))
          case ex =>
            IO.raiseError(ex)
        }
    }

    response.mapError {
      case NonFatal(ex) =>
        Logging.requestFailed(cId, _)
        KeycloakThrowable(ex)
    }
  }

  /* REST Protocol Calls **/
  def get[A : Anything](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue]): IO[KeycloakError, A] = {
    val request = basicRequest.get(createUri(path, query))
    call[A](request, buildRequestInfo(path, "GET", ()))
  }

  def put[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): IO[KeycloakError, A] = {
    val request   = basicRequest.put(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "PUT", injected.body))
  }

  def post[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): IO[KeycloakError, A] = {
    val request   = basicRequest.post(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "POST", injected.body))
  }

  def delete[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): IO[KeycloakError, A] = {
    val request   = basicRequest.delete(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "DELETE", injected.body))
  }
}

object KeycloakClient {
  type Headers = Map[String, String]
}
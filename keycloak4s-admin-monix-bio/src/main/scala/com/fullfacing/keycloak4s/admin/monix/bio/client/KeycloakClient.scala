package com.fullfacing.keycloak4s.admin.monix.bio.client

import com.fullfacing.keycloak4s.admin.monix.bio.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.admin.monix.bio.client.implicits.{Anything, BodyMagnet}
import com.fullfacing.keycloak4s.admin.monix.bio.handles.Logging
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import monix.bio.{IO, UIO}
import org.json4s.jackson.Serialization.read
import sttp.client3.{Identity, RequestT, Response, SttpBackend, asString, basicRequest}
import sttp.model.Uri.QuerySegment.KeyValue
import sttp.model.{StatusCode, Uri}

import java.util.UUID
import scala.collection.immutable.Seq
import scala.reflect.Manifest
import scala.reflect.runtime.universe._

class KeycloakClient(config: ConfigWithAuth)(implicit client: SttpBackend[IO[Throwable, *], Any]) extends TokenManager(config) {

  val realm: String = config.realm

  /* Extracts the Manifest out of an implicit Anything. **/
  private implicit def ma[A : Anything]: Manifest[A] = implicitly[Anything[A]].manifest

  /* URI Builder **/
  private[client] def createUri(path: Seq[String], query: Seq[KeyValue]) = Uri.apply(
    scheme         = config.scheme,
    userInfo       = None,
    host           = config.host,
    port           = Some(config.port),
    path           = config.basePath ++ Seq("admin", "realms") ++ path,
    querySegments  = query,
    fragment       = None
  )

  /* HTTP Call Builders **/

  private def setResponse[A <: Any : Manifest](request: RequestT[Identity, Either[String, String], Any])
                                              (implicit tag: TypeTag[A], cId: UUID)
  : RequestT[Identity, Either[String, A], Any] = {

    val respAs = asString.mapWithMetadata { case (raw, meta) =>
      raw.map { body =>
        Logging.requestSuccessful(body, cId)

        if (tag.tpe =:= typeOf[Unit]) read[A]("null")
        else if (tag.tpe =:= typeOf[Headers]) meta.headers.map(h => h.name -> h.value).toMap.asInstanceOf[A]
        else read[A](body)
      }
    }

    request.response(respAs)
  }

  private def call[B <: Any : Manifest](request: RequestT[Identity, Either[String, String], Any], requestInfo: RequestInfo): IO[KeycloakError, B] = {
    implicit val cId: UUID = UUID.randomUUID()

    val resp = setResponse[B](request.header("Accept", "application/json"))

    def sendWithLogging(req: RequestT[Identity, Either[String, B], Any]): IO[Throwable, Response[Either[String, B]]] = {
      UIO(Logging.requestSent(requestInfo, cId)).flatMap(_ => req.send(client))
    }

    def retryWithLogging(req: RequestT[Identity, Either[String, B], Any]): IO[Throwable, Response[Either[String, B]]] = {
      UIO(Logging.retryUnauthorized(requestInfo, cId)).flatMap(_ => req.send(client))
    }

    (for {
      auth <- withAuth(resp)
      res  <- sendWithLogging(auth)
      b    <- liftM(res, requestInfo)
    } yield b)
      .onErrorRecoverWith {
        case KeycloakSttpException(StatusCode.Unauthorized.code, _, _, _, _) =>
          for {
            token <- withAuthNewToken(resp)
            retry <- retryWithLogging(token)
            b     <- liftM(retry, requestInfo)
          } yield b
      }
      .mapError { ex =>
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
package com.fullfacing.keycloak4s.admin.client

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits._
import com.fullfacing.keycloak4s.admin.Logging
import com.fullfacing.keycloak4s.admin.Logging.logLeft
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.admin.implicits.{Anything, BodyMagnet}
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization.read
import sttp.client.{Identity, NothingT, RequestT, SttpBackend, asString, _}
import sttp.model.Uri.QuerySegment.KeyValue
import sttp.model.{StatusCode, Uri}

import java.util.UUID
import scala.collection.immutable.Seq
import scala.reflect._
import scala.reflect.runtime.universe.{TypeTag, typeOf}
import scala.util.control.NonFatal

class KeycloakClient[F[+_] : Concurrent, -S](config: ConfigWithAuth)(implicit client: SttpBackend[F, S, NothingT]) extends TokenManager[F, S](config) {

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
  private def setResponse[A <: Any : Manifest](request: RequestT[Identity, Either[String, String], Nothing])
                                              (implicit tag: TypeTag[A], cId: UUID)
  : RequestT[Identity, Either[String, A], Nothing] = {

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

  private def call[B <: Any : Manifest](request: RequestT[Identity, Either[String, String], Nothing], requestInfo: RequestInfo): F[Either[KeycloakError, B]] = {
    implicit val cId: UUID = UUID.randomUUID()

    val resp = setResponse[B](request.header("Accept", "application/json"))

    def sendWithLogging(req: RequestT[Identity, Either[String, B], Nothing]) = {
      F.unit(Logging.requestSent(requestInfo, cId))
        .flatMap(_ => req.send())
    }

    def retryWithLogging(req: RequestT[Identity, Either[String, B], Nothing]) = {
      F.unit(Logging.retryUnauthorized(requestInfo, cId))
        .flatMap(_ => req.send())
    }

    val response = EitherT(withAuth(resp)).flatMap { r =>
      EitherT(F.map(sendWithLogging(r))(liftM(_, requestInfo)))
        .leftFlatMap {
          case KeycloakSttpException(StatusCode.Unauthorized.code, _, _, _, _) =>
            EitherT(withAuthNewToken(resp))
              .flatMapF(r => F.map(retryWithLogging(r))(liftM(_, requestInfo)))
          case ex =>
            EitherT(F.unit(ex.asLeft[B]))
        }
    }

    F.handleError[Either[KeycloakError, B]](response.value) {
      case NonFatal(ex) => F.unit(KeycloakThrowable(ex).asLeft[B])
    }.map(logLeft(_)(Logging.requestFailed(cId, _)))
  }

  /* REST Protocol Calls **/
  def get[A : Anything](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request = basicRequest.get(createUri(path, query))
    call[A](request, buildRequestInfo(path, "GET", ()))
  }

  def put[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = basicRequest.put(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "PUT", injected.body))
  }

  def post[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = basicRequest.post(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "POST", injected.body))
  }

  def delete[A : Anything](path: Seq[String], payload: BodyMagnet = (), query: Seq[KeyValue] = Seq.empty[KeyValue]): F[Either[KeycloakError, A]] = {
    val request   = basicRequest.delete(createUri(path, query))
    val injected  = payload.apply(request)
    call[A](injected, buildRequestInfo(path, "DELETE", injected.body))
  }
}

object KeycloakClient {
  type Headers = Map[String, String]
}
package com.fullfacing.transport.api

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToResponseMarshaller}
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.util.ByteString
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.keycloak4s.core.models.enums.EventTypes
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.fullfacing.transport.Config._
import com.fullfacing.transport.Implicits._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

import java.util.UUID

object ClientsApi extends SecurityDirectives {

  implicit def toResponseMarshaller[A <: AnyRef : Manifest]: ToResponseMarshaller[A] = {
    Marshaller.strict[A, HttpResponse] { body =>
      Marshalling.WithFixedContentType(
        contentType = MediaTypes.`application/json`,
        marshal     = () => HttpResponse(StatusCodes.OK, entity = HttpEntity.Strict(MediaTypes.`application/json`, ByteString(Serialization.write(body))))
      )
    }
  }

  case class Response(deprecated: List[String],
                      newInKeycloak: List[String])

  lazy val api: Route = {
    get {
      path("compare") {
        entity(as[String]) { entity =>
          val list1 = read[List[String]](entity)
          val list2 = EventTypes.values.map(_.value).toList

          val r = Response(
            (list2 diff list1).sortWith(_ > _),
            (list1 diff list2).sortWith(_ > _)
          )

          complete(r)
        }
      }
    } ~
    contextFromPostman { correlationId =>
      secure((pathClientsConfig, correlationId)) { _ =>
          SiteRoutes.api
      }
    }
  }

  def contextFromPostman: Directive1[UUID] = {
    optionalHeaderValueByName("Postman-Token").flatMap { cId =>
      provide {
        cId.fold(UUID.randomUUID())(UUID.fromString)
      }
    }
  }
}
package com.fullfacing.keycloak4s.admin.client.implicits

import com.fullfacing.keycloak4s.core.models.enums.ContentTypes
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{Id, Multipart, Request, RequestT}
import org.json4s.jackson.Serialization

sealed trait BodyMagnet {
  def apply: Request[String, Nothing] => RequestT[Id, String, Nothing]
}

object BodyMagnet {
  private implicit val serialization: Serialization.type = org.json4s.jackson.Serialization

  type Result = Request[String, Nothing] => RequestT[Id, String, Nothing]

  implicit def fromMap(m: Map[Any, Any]): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.UrlEncoded).body(m)
  }

  implicit def fromMultipart(mp: Multipart): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.Multipart).body(mp)
  }

  implicit def fromUnit(u: Unit): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request
  }

  implicit def fromAnyRef[A <: AnyRef](a: A): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.Json).body(a)
  }
}

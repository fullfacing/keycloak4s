package com.fullfacing.keycloak4s.admin.client.implicits

import com.fullfacing.keycloak4s.core.models.enums.ContentTypes
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization
import sttp.client.{BasicRequestBody, Identity, Request, RequestT, _}
import sttp.model.Part
import sttp.client.json4s._

sealed trait BodyMagnet {
  def apply: Request[Either[String, String], Nothing] => RequestT[Identity, Either[String, String], Nothing]
}

object BodyMagnet {

  type Result = Request[Either[String, String], Nothing] => RequestT[Identity, Either[String, String], Nothing]

  implicit def fromMap(m: Map[Any, Any]): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.UrlEncoded).body(m.map(a => a._1.toString -> a._2.toString))
  }

  implicit def fromMultipart(mp: Part[BasicRequestBody]): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.Multipart).multipartBody(mp)
  }

  implicit def fromUnit(u: Unit): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request
  }

  implicit def plainText(text: String): BodyMagnet = new BodyMagnet {
     def apply: Result = request => request.contentType(ContentTypes.TextPlain).body(text)
  }

  implicit def fromAnyRef[A <: AnyRef](a: A): BodyMagnet = new BodyMagnet {
    def apply: Result = request => request.contentType(ContentTypes.Json).body(Serialization.write(a))
  }
}

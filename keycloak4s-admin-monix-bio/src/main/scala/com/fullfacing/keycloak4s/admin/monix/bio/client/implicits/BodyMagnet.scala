package com.fullfacing.keycloak4s.admin.monix.bio.client.implicits

import com.fullfacing.keycloak4s.core.models.enums.ContentTypes
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization
import sttp.client.{BasicRequestBody, Identity, Request, RequestT}
import sttp.model.Part

sealed trait BodyMagnet {
  def apply: Request[Either[String, String], Nothing] => RequestT[Identity, Either[String, String], Nothing]
}

object BodyMagnet {

  type Result = Request[Either[String, String], Nothing] => RequestT[Identity, Either[String, String], Nothing]

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

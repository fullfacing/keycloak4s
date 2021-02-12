package com.fullfacing.keycloak4s.authz.monix.bio.serialization

import com.fullfacing.keycloak4s.authz.monix.bio.models.IntrospectionResponse
import com.fullfacing.keycloak4s.core.serialization.JsonFormats
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.{Formats, JValue, Serializer, TypeInfo}

class IntrospectionSerializer extends Serializer[IntrospectionResponse] {
  private val ParentClass = classOf[IntrospectionResponse]

  private implicit val formats: Formats = JsonFormats.default

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), IntrospectionResponse] = {
    case (TypeInfo(ParentClass, _), json) =>
      val aud   = (json \ "aud").extract[JValue](format, manifest)
      val token = json.extract[IntrospectionResponse.WithoutAud](format, manifest)
      aud match {
        case JArray(arr) => IntrospectionResponse(token, arr.collect { case JString(s) => s })
        case JString(s)  => IntrospectionResponse(token, s :: Nil)
        case _           => IntrospectionResponse(token, List.empty[String])
      }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = Map()
}

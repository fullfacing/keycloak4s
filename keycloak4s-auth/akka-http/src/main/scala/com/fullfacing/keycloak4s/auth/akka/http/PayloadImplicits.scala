package com.fullfacing.keycloak4s.auth.akka.http

import com.fullfacing.keycloak4s.auth.akka.http.models.AuthRoles
import com.nimbusds.jose.Payload

import scala.util.Try
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.Formats
import org.json4s.jackson.Serialization.read

object PayloadImplicits {
  private def safeExtract(payload: Payload, key: String): Option[String] = Try {
    payload.toJSONObject.getAsString(key)
  }.toOption

  implicit class PayloadImpl(payload: Payload) {
    def extract(key: String): Option[String] =
      safeExtract(payload, key)

    def extractAs[A : Manifest](key: String)(implicit formats: Formats = default): Option[A] =
      safeExtract(payload, key).map(read[A](_)(formats, manifest))

    def extractEmail: Option[String] =
      safeExtract(payload, "email")

    def extractFirstName: Option[String] =
      safeExtract(payload, "given_name")

    def extractSurname: Option[String] =
      safeExtract(payload, "family_name")

    def extractFullName: Option[String] =
      safeExtract(payload, "name")

    def extractUsername: Option[String] =
      safeExtract(payload, "preferred_username")

    def extractResources: Map[String, AuthRoles] =
      extractAs[Map[String, AuthRoles]]("resource_access").getOrElse(Map.empty[String, AuthRoles])
  }
}

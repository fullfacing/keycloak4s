package com.fullfacing.keycloak4s.auth.akka.http

import com.fullfacing.keycloak4s.auth.akka.http.models.AuthRoles
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.nimbusds.jose.Payload
import org.json4s.Formats
import org.json4s.jackson.Serialization.read

/**
 * Provides helper functions to safely extract values from any Payload object.
 * Note: Only JSON extraction is executed safely, deserialization is not safe and can throw exceptions.
 */
object PayloadImplicits {

  /* A safe extraction method to extract any field's value from a Payload. **/
  private def safeExtract(payload: Payload, key: String): Option[String] = Option {
    payload.toJSONObject.getAsString(key)
  }

  implicit class PayloadImpl(payload: Payload) {

    /* Generic Extractors. **/

    def extract(key: String): Option[String] =
      safeExtract(payload, key)

    def extractList(key: String): List[String] =
      safeExtract(payload, key).map(read[List[String]](_)).getOrElse(List.empty[String])

    def extractAs[A : Manifest](key: String)(implicit formats: Formats = default): Option[A] =
      safeExtract(payload, key).map(read[A](_)(formats, manifest))

    def extractAsListOf[A : Manifest](key: String)(implicit formats: Formats = default): List[A] =
      safeExtract(payload, key).map(read[List[A]](_)(formats, manifest)).getOrElse(List.empty[A])

    /* User Info Extractors. **/

    def extractEmail: Option[String] =
      safeExtract(payload, "email")

    def extractEmailVerified: Option[Boolean] =
      extractAs[Boolean]("email_verified")

    def extractUsername: Option[String] =
      safeExtract(payload, "preferred_username")

    def extractFirstName: Option[String] =
      safeExtract(payload, "given_name")

    def extractSurname: Option[String] =
      safeExtract(payload, "family_name")

    def extractFullName: Option[String] =
      safeExtract(payload, "name")

    //TODO Add phone number and phone number verified extractors

    /* Access Control Extractors. **/

    def extractResourceAccess: Map[String, AuthRoles] =
      extractAs[Map[String, AuthRoles]]("resource_access").getOrElse(Map.empty[String, AuthRoles])

    def extractResourceRoles(resource: String): List[String] =
      extractResourceAccess.get(resource).fold(List.empty[String])(_.roles)

    def extractRealmAccess: Option[AuthRoles] =
      extractAs[AuthRoles]("realm_access")

    def extractRealmRoles: List[String] =
      extractRealmAccess.fold(List.empty[String])(_.roles)

    def extractScopes: List[String] =
      extract("scope").fold(List.empty[String])(_.split(" ").toList)
  }
}

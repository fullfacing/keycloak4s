package com.fullfacing.keycloak4s.client.serialization

import com.fullfacing.keycloak4s.models.Credential.CredentialTypes
import com.fullfacing.keycloak4s.models.User.RequiredActions
import enumeratum.values.Json4s
import org.json4s.Formats

object JsonFormats {
  implicit val default: Formats = org.json4s.DefaultFormats + UuidSerializer +
    Json4s.serializer(RequiredActions) + Json4s.serializer(CredentialTypes)
}

package com.fullfacing.keycloak4s.core.serialization

import org.json4s.Formats

object JsonFormats {
  implicit val default: Formats = org.json4s.DefaultFormats + UuidSerializer
}

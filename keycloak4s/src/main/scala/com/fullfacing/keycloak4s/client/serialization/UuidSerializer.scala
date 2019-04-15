package com.fullfacing.keycloak4s.client.serialization

import java.util.UUID

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

object UuidSerializer extends CustomSerializer[UUID](_ => (
  {
    case JString(s) => UUID.fromString(s)
    case JNull => null
  },
  {
    case d: UUID => JString(d.toString)
  }
))
package com.fullfacing.keycloak4s.admin.serialization

import com.fullfacing.keycloak4s.core.serialization.JsonFormats.{default => coreDefault}
import org.json4s.Formats

object JsonFormats {
  implicit val default: Formats = coreDefault ++ EnumSerializers.all
}

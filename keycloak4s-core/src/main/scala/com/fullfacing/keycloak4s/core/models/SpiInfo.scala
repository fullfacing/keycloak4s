package com.fullfacing.keycloak4s.core.models

final case class SpiInfo(internal: Option[Boolean],
                         providers: Option[Map[String, Any]])

package com.fullfacing.keycloak4s.admin.models

final case class SpiInfo(internal: Option[Boolean],
                         providers: Option[Map[String, Any]])

package com.fullfacing.keycloak4s.core.models

final case class UserSession(clients: Option[Map[String, String]],
                             id: Option[String],
                             ipAddress: Option[String],
                             lastAccess: Option[String],
                             start: Option[String],
                             userId: Option[String],
                             username: Option[String])

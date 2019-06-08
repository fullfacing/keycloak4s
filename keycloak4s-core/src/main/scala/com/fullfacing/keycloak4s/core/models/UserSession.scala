package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class UserSession(clients: Option[Map[String, String]],
                             id: UUID,
                             ipAddress: String,
                             lastAccess: String,
                             start: String,
                             userId: String,
                             username: String)

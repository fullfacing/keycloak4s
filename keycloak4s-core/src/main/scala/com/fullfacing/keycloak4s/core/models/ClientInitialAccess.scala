package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class ClientInitialAccess(count: Int,
                                     expiration: Int,
                                     id: UUID,
                                     remainingCount: Int,
                                     timestamp: Long,
                                     token: Option[String])

object ClientInitialAccess {
  final case class Create(count: Option[Int],
                          expiration: Option[Long])
}

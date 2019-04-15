package com.fullfacing.keycloak4s.models

final case class ClientInitialAccess(count: Option[Int],
                                     expiration: Option[Int],
                                     id: Option[String],
                                     remainingCount: Option[Int],
                                     timestamp: Option[Int],
                                     token: Option[String])

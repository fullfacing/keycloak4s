package com.fullfacing.keycloak4s.core.models

final case class ClientInitialAccessCreate(count: Option[Int],
                                           expiration: Option[Int])

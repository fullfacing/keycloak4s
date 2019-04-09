package com.fullfacing.keycloak4s.models

final case class ClientInitialAccessCreate(count: Option[Int],
                                           expiration: Option[Int])

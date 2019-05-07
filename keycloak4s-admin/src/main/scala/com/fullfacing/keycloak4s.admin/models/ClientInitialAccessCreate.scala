package com.fullfacing.keycloak4s.admin.models

final case class ClientInitialAccessCreate(count: Option[Int],
                                           expiration: Option[Int])

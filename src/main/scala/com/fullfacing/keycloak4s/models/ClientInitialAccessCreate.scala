package com.fullfacing.keycloak4s.models

case class ClientInitialAccessCreate(
                                      count: Option[Int],
                                      expiration: Option[Int]
                                    )

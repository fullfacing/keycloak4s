package com.fullfacing.keycloak4s.admin.models

import java.time.Instant

sealed trait Token {
  val access: String
  val authenticateAt: Instant
}

final case class TokenWithoutRefresh(access: String,
                                     authenticateAt: Instant) extends Token

final case class TokenWithRefresh(access: String,
                                  refresh: String,
                                  refreshAt: Instant,
                                  authenticateAt: Instant) extends Token
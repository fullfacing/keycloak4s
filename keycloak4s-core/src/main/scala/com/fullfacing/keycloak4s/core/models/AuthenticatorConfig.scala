package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class AuthenticatorConfig(alias: String,
                                     config: Map[String, String],
                                     id: UUID)

object AuthenticatorConfig {
  final case class Create(alias: String,
                          config: Option[Map[String, String]] = None)

  final case class Update(alias: Option[String] = None,
                          config: Option[Map[String, String]] = None)
}

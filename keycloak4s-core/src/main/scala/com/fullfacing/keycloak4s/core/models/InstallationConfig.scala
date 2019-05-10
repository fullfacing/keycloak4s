package com.fullfacing.keycloak4s.core.models

import java.util.UUID

case class InstallationConfig(realm: String,
                              `auth-server-url`: String,
                              `ssl-required`: String,
                              resource: String,
                              `verify-token-audience`: Option[Boolean],
                              credentials: Option[Credentials],
                              `use-resource-role-mappings`: Option[Boolean],
                              `confidential-port`: Int,
                              `policy-enforcer`: Option[Map[Any, Any]])

case class Credentials(secret: UUID)
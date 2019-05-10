package com.fullfacing.keycloak4s.core.models

final case class ManagementPermission(enabled: Option[Boolean],
                                      resource: Option[String],
                                      scopePermissions: Option[Map[String, String]])

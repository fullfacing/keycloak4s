package com.fullfacing.keycloak4s.models

case class ManagementPermission(enabled: Option[Boolean],
                                resource: Option[String],
                                scopePermissions: Option[Map[_, _]])

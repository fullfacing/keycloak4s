package com.fullfacing.keycloak4s.core.models

final case class Permission(claims: Option[Map[String, Any]],
                            rsid: Option[String],
                            rsname: Option[String],
                            scopes: Option[List[String]])

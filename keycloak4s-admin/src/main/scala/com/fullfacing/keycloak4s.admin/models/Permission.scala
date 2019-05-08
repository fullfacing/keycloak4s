package com.fullfacing.keycloak4s.admin.models

final case class Permission(claims: Option[Map[String, Any]],
                            rsid: Option[String],
                            rsname: Option[String],
                            scopes: Option[List[String]])

package com.fullfacing.keycloak4s.models

case class Permission(claims: Option[Map[String, Any]],
                      rsid: Option[String],
                      rsname: Option[String],
                      scopes: Option[List[String]])

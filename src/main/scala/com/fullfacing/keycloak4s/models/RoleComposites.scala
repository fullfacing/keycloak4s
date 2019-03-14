package com.fullfacing.keycloak4s.models

case class RoleComposites(client: Option[Map[String, Role]],
                          realm: Option[List[String]])

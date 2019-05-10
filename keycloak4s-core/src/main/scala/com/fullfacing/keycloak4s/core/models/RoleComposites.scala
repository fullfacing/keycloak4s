package com.fullfacing.keycloak4s.core.models

final case class RoleComposites(client: Map[String, Role] = Map.empty,
                                realm: List[String])

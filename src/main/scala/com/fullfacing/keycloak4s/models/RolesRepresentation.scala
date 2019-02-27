package com.fullfacing.keycloak4s.models

case class RolesRepresentation(client: Option[Map[_, _]],
                               realm: Option[List[RolesRepresentation]])

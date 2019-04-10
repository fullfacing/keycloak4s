package com.fullfacing.keycloak4s.models

final case class RolesRepresentation(client: Option[Map[String, Role]],
                                     realm: Option[Role])

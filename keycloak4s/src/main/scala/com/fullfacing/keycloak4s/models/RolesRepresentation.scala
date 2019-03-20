package com.fullfacing.keycloak4s.models

case class RolesRepresentation(client: Option[Map[String, Role]],
                               realm: Option[Role])

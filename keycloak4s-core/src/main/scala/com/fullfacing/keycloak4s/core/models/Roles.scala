package com.fullfacing.keycloak4s.core.models

final case class Roles(client: Option[Map[String, Role]],
                       realm: Option[Role])

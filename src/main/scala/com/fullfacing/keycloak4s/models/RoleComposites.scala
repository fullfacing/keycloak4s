package com.fullfacing.keycloak4s.models

case class RoleComposites(
                           client: Option[Map[_, _]],
                           realm: Option[List[String]]
                         )

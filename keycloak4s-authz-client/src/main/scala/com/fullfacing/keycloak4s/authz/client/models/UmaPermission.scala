package com.fullfacing.keycloak4s.authz.client.models

final case class UmaPermission(roles: List[String],
                               groups: List[String],
                               clients: List[String],
                               users: List[String],
                               condition: String,
                               id: String)

object UmaPermission {
  final case class Create(roles: List[String],
                          groups: List[String],
                          clients: List[String],
                          users: List[String],
                          condition: String)

  final case class Update(id: String,
                          roles: Option[List[String]] = None,
                          groups: Option[List[String]] = None,
                          clients: Option[List[String]] = None,
                          users: Option[List[String]] = None,
                          condition: Option[List[String]] = None)
}

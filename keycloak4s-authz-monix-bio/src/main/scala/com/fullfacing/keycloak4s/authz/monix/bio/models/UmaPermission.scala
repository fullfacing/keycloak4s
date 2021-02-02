package com.fullfacing.keycloak4s.authz.monix.bio.models

import com.fullfacing.keycloak4s.core.models.enums.{Logic, LogicTypes}

final case class UmaPermission(name: String,
                               description: Option[String],
                               `type`: String,
                               policies: List[String] = List.empty[String],
                               resources: List[String] = List.empty[String],
                               logic: Logic,
                               owner: String,
                               scopes: List[String] = List.empty[String],
                               roles: List[String] = List.empty[String],
                               groups: List[String] = List.empty[String],
                               clients: List[String] = List.empty[String],
                               users: List[String] = List.empty[String],
                               id: String)

object UmaPermission {

  final case class Create(name: String,
                          description: Option[String],
                          `type`: String,
                          policies: List[String] = List.empty[String],
                          resources: List[String] = List.empty[String],
                          logic: Logic = LogicTypes.Positive,
                          scopes: List[String] = List.empty[String],
                          roles: List[String] = List.empty[String],
                          groups: List[String] = List.empty[String],
                          clients: List[String] = List.empty[String],
                          users: List[String] = List.empty[String])
}

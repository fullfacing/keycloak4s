package com.fullfacing.keycloak4s.authz.monix.bio.models

import com.fullfacing.keycloak4s.core.models.enums.{DecisionStrategy, Logic}

final case class UmaPermission(name: String,
                               description: Option[String],
                               `type`: String,
                               policies: Option[List[String]] = None,
                               resources: Option[List[String]] = None,
                               logic: Logic,
                               decisionStrategy: DecisionStrategy,
                               owner: String,
                               scopes: List[String] = List.empty[String],
                               roles: Option[List[String]] = None,
                               groups: Option[List[String]] = None,
                               clients: Option[List[String]] = None,
                               users: Option[List[String]] = None,
                               id: String)

object UmaPermission {

  final case class Create(name: String,
                          `type`: Option[String] = None,
                          description: Option[String] = None,
                          policies: List[String] = List.empty[String],
                          resources: List[String] = List.empty[String],
                          logic: Option[Logic] = None,
                          decisionStrategy: Option[DecisionStrategy] = None,
                          scopes: List[String] = List.empty[String],
                          roles: List[String] = List.empty[String],
                          groups: List[String] = List.empty[String],
                          clients: List[String] = List.empty[String],
                          users: List[String] = List.empty[String])
}

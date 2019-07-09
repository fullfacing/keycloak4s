package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class Group(name: String,
                       path: String,
                       subGroups: List[Group] = List.empty[Group],
                       realmRoles: List[String] = List.empty[String],
                       access: Map[String, Boolean] = Map.empty[String, Boolean],
                       attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                       clientRoles: Map[String, List[String]] = Map.empty[String, List[String]],
                       id: UUID)

object Group {

  final case class Create(name: String,
                          attributes: Map[String, List[String]] = Map.empty[String, List[String]])

  final case class Update(name: Option[String] = None,
                          attributes: Option[Map[String, List[String]]] = None)
}
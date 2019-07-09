package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class Role(attributes: Map[String, Seq[String]] = Map.empty[String, Seq[String]],
                      clientRole: Boolean,
                      composite: Boolean,
                      composites: Option[RoleComposites] = None,
                      containerId: Option[String] = None,
                      description: Option[String] = None,
                      id: UUID,
                      name: String)

object Role {

  final case class Create(clientRole: Boolean,
                          composite: Boolean,
                          composites: Option[RoleComposites] = None,
                          containerId: Option[String] = None,
                          description: Option[String] = None,
                          name: String)

  /** Testing revealed name must be included in the update body */
  final case class Update(attributes: Option[Map[String, Seq[String]]] = None,
                          containerId: Option[String] = None,
                          description: Option[String] = None,
                          name: String)

  final case class Mapping(id: UUID,
                           name: String)
}
package com.fullfacing.keycloak4s.admin.models

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

  final case class Update(attributes: Option[Map[String, Seq[String]]] = None,
                          clientRole: Option[Boolean] = None,
                          composite: Option[Boolean] = None,
                          composites: Option[RoleComposites] = None,
                          containerId: Option[String] = None,
                          description: Option[String] = None)

  final case class Mapping(id: Option[UUID] = None,
                           name: Option[String] = None)
}
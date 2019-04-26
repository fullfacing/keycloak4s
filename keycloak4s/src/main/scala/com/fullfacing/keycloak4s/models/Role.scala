package com.fullfacing.keycloak4s.models

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

  final case class Create(clientRole: Boolean = false,
                          composite: Boolean = false,
                          composites: Option[RoleComposites] = None,
                          containerId: Option[String] = None,
                          description: Option[String] = None,
                          name: String)
}
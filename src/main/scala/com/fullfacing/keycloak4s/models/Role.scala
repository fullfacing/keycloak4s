package com.fullfacing.keycloak4s.models

final case class Role(attributes: Option[Map[_, _]],
                      clientRole: Option[Boolean],
                      composite: Option[Boolean],
                      composites: Option[RoleComposites],
                      containerId: Option[String],
                      description: Option[String],
                      id: Option[String],
                      name: Option[String])

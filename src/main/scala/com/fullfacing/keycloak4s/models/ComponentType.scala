package com.fullfacing.keycloak4s.models

case class ComponentType(helpText: Option[String],
                         id: Option[String],
                         metadata: Option[Map[_, _]],
                         properties: Option[ConfigProperty])
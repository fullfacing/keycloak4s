package com.fullfacing.keycloak4s.core.models

final case class ComponentType(helpText: Option[String],
                               id: Option[String],
                               metadata: Option[Map[String, AnyRef]],
                               properties: Option[ConfigProperty])
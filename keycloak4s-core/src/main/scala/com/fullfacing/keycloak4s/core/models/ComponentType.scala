package com.fullfacing.keycloak4s.core.models

final case class ComponentType(helpText: String,
                               id: String,
                               metadata: Option[Map[String, AnyRef]],
                               properties: Option[ConfigProperty])
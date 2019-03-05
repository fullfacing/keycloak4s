package com.fullfacing.keycloak4s.models

case class ConfigProperty(defaultValue: AnyRef,
                          helpText: Option[String],
                          label: Option[String],
                          name: Option[String],
                          options: Option[List[String]],
                          secret: Option[Boolean],
                          `type`: Option[String])

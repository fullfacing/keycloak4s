package com.fullfacing.keycloak4s.core.models

final case class RequiredActionProvider(alias: String,
                                        config: Map[String, AnyRef],
                                        defaultAction: Boolean,
                                        enabled: Boolean,
                                        name: String,
                                        priority: Int,
                                        providerId: Option[String])

object RequiredActionProvider {
  final case class Update(alias: String,
                          config: Map[String, AnyRef],
                          defaultAction: Option[Boolean] = None,
                          enabled: Option[Boolean] = None,
                          name: String,
                          priority: Int)
}

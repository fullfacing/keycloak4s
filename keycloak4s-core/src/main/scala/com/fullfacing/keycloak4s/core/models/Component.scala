package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class Component(config: Map[String, Seq[String]] = Map.empty,
                           id: UUID,
                           name: Option[String],
                           parentId: Option[String],
                           providerId: Option[String],
                           providerType: Option[String],
                           subType: Option[String])

object Component {
  final case class Create(config: Map[String, Seq[String]] = Map.empty,
                          name: Option[String] = None,
                          parentId: Option[String] = None,
                          providerId: Option[String] = None,
                          providerType: Option[String] = None,
                          subType: Option[String] = None)

  final case class Update(config: Option[Map[String, Seq[String]]] = None,
                          name: Option[String] = None,
                          parentId: Option[String] = None,
                          providerId: Option[String] = None,
                          providerType: Option[String] = None,
                          subType: Option[String] = None)
}
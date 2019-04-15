package com.fullfacing.keycloak4s.models

final case class Component(config: Option[Map[String, Seq[String]]],
                           id: Option[String],
                           name: Option[String],
                           parentId: Option[String],
                           providerId: Option[String],
                           providerType: Option[String],
                           subType: Option[String])
package com.fullfacing.keycloak4s.models

import java.util.UUID

import com.fullfacing.keycloak4s.models.enums.Protocol

final case class ProtocolMapper(config: Map[String, String] = Map.empty,
                                id: UUID,
                                name: String,
                                protocol: Protocol,
                                protocolMapper: String,
                                consentRequired: Boolean)

object ProtocolMapper {

  final case class Create(config: Map[String, String] = Map.empty,
                          name: String,
                          protocol: Protocol,
                          protocolMapper: String,
                          consentRequired: Boolean)

  final case class Update(config: Option[Map[String, String]] = None,
                          name: Option[String] = None,
                          protocol: Option[Protocol] = None,
                          protocolMapper: Option[String] = None,
                          consentRequired: Option[Boolean] = None)
}

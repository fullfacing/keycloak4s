package com.fullfacing.keycloak4s.core.models

import java.util.UUID

import com.fullfacing.keycloak4s.core.models.enums.Protocol

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

  /**
   * NB: Based on testing, only the properties in the config field can be changed.
   * The rest of this object should populated with its existing values.
   */
  final case class Update(id: UUID,
                          config: Option[Map[String, String]] = None,
                          protocol: Protocol,
                          protocolMapper: String)
}

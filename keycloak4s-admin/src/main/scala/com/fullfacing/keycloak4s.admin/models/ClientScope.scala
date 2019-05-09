package com.fullfacing.keycloak4s.admin.models

import java.util.UUID

import com.fullfacing.keycloak4s.admin.models.enums.{Protocol, Protocols}

final case class ClientScope(id: UUID,
                             name: String,
                             attributes: Map[String, String] = Map.empty[String, String],
                             description: Option[String],
                             protocol: Protocol,
                             protocolMappers: List[ProtocolMapper])

object ClientScope {

  final case class Create(name: String,
                          attributes: Map[String, String] = Map.empty[String, String],
                          description: Option[String] = None,
                          protocol: Protocol = Protocols.OpenIdConnect,
                          protocolMappers: List[ProtocolMapper] = List.empty[ProtocolMapper])

  final case class Update(name: Option[String] = None,
                          attributes: Option[Map[String, String]] = None,
                          description: Option[String] = None,
                          protocol: Option[Protocol] = None,
                          protocolMappers: Option[List[ProtocolMapper]] = None)
}

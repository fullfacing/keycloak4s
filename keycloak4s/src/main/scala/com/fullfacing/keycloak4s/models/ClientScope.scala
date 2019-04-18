package com.fullfacing.keycloak4s.models

import java.util.UUID

final case class ClientScope(id: UUID,
                             name: String,
                             attributes: Map[String, String] = Map.empty[String, String],
                             description: Option[String],
                             protocol: Client.Protocol,
                             protocolMappers: List[ProtocolMapper])

object ClientScope {

  final case class Create(name: String,
                          attributes: Map[String, String] = Map.empty[String, String],
                          description: Option[String] = None,
                          protocol: Client.Protocol = Client.Protocols.OpenIdConnect,
                          protocolMappers: List[ProtocolMapper] = List.empty[ProtocolMapper])

  final case class Update(name: Option[String] = None,
                          attributes: Option[Map[String, String]] = None,
                          description: Option[String] = None,
                          protocol: Option[Client.Protocol] = None,
                          protocolMappers: Option[List[ProtocolMapper]] = None)
}

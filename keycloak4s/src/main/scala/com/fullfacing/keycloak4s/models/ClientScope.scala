package com.fullfacing.keycloak4s.models

final case class ClientScope(attributes: Option[Map[String, String]],
                             description: Option[String],
                             id: Option[String],
                             name: Option[String],
                             protocol: Option[String],
                             protocolMappers: Option[List[ProtocolMapper]])

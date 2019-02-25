package com.fullfacing.keycloak4s.models

case class ClientScope(
                        attributes: Option[Map[_, _]],
                        description: Option[String],
                        id: Option[String],
                        name: Option[String],
                        protocol: Option[String],
                        protocolMappers: Option[List[ProtocolMapper]]
                      )

package com.fullfacing.keycloak4s.models

case class ProtocolMapper(config: Option[Map[_, _]],
                          id: Option[String],
                          name: Option[String],
                          protocol: Option[String],
                          protocolMapper: Option[String])

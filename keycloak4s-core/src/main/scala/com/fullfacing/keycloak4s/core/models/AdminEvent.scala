package com.fullfacing.keycloak4s.core.models

final case class AdminEvent(authDetails: Option[AdminEvent.AuthDetails],
                            error: Option[String],
                            operationType: Option[String],
                            realmId: Option[String],
                            representation: Option[String],
                            resourcePath: Option[String],
                            resourceType: Option[String],
                            time: Option[Long])

object AdminEvent {
  final case class AuthDetails(clientId: Option[String],
                               ipAddress: Option[String],
                               realmId: Option[String],
                               userId: Option[String])
}

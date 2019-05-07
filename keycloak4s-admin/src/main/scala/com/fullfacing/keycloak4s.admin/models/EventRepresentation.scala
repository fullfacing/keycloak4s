package com.fullfacing.keycloak4s.admin.models

final case class EventRepresentation(clientId: Option[String],
                                     details: Option[Map[String, String]],
                                     error: Option[String],
                                     ipAddress: Option[String],
                                     realmId: Option[String],
                                     time: Option[Long],
                                     `type`: Option[String],
                                     userId: Option[String])

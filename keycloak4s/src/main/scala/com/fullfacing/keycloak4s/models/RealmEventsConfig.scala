package com.fullfacing.keycloak4s.models

final case class RealmEventsConfig(adminEventsDetailsEnabled: Option[Boolean],
                                   adminEventsEnabled: Option[Boolean],
                                   enabledEventTypes: Option[List[String]],
                                   eventsEnabled: Option[Boolean],
                                   eventsExpiration: Option[Long],
                                   eventsListeners: Option[List[String]])

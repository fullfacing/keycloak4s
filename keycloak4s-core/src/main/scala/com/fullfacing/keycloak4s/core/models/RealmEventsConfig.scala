package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.EventType

final case class RealmEventsConfig(adminEventsDetailsEnabled: Boolean,
                                   adminEventsEnabled: Boolean,
                                   enabledEventTypes: List[EventType],
                                   eventsEnabled: Boolean,
                                   eventsExpiration: Option[Long],
                                   eventsListeners: List[String])

object RealmEventsConfig {
  final case class Update(adminEventsDetailsEnabled: Option[Boolean],
                          adminEventsEnabled: Option[Boolean],
                          enabledEventTypes: List[EventType] = List.empty[EventType],
                          eventsEnabled: Option[Boolean],
                          eventsExpiration: Option[Long],
                          eventsListeners: List[String] = List.empty[String])
}
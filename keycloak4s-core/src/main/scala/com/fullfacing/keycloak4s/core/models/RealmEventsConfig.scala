package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.EventType

final case class RealmEventsConfig(adminEventsDetailsEnabled: Boolean,
                                   adminEventsEnabled: Boolean,
                                   enabledEventTypes: List[EventType],
                                   eventsEnabled: Boolean,
                                   eventsExpiration: Option[Long],
                                   eventsListeners: List[String])

object RealmEventsConfig {
  final case class Update(adminEventsDetailsEnabled: Option[Boolean] = None,
                          adminEventsEnabled: Option[Boolean] = None,
                          enabledEventTypes: Option[List[EventType]] = None,
                          eventsEnabled: Option[Boolean] = None,
                          eventsExpiration: Option[Long] = None,
                          eventsListeners: Option[List[String]] = None)
}
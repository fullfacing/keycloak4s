package models

case class RealmEventsConfig(
                              adminEventsDetailsEnabled: Option[Boolean],
                              adminEventsEnabled: Option[Boolean],
                              enabledEventTypes: Option[List[String]],
                              eventsEnabled: Option[Boolean],
                              eventsExpiration: Option[Long],
                              eventsListeners: Option[List[String]]
                            )

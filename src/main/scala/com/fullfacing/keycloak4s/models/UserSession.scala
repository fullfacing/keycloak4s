package models

case class UserSession(
                        clients: Option[Map[_, _]],
                        id: Option[String],
                        ipAddress: Option[String],
                        lastAccess: Option[String],
                        start: Option[String],
                        userId: Option[String],
                        username: Option[String]
                      )

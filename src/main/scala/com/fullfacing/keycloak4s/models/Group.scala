package models

case class Group(
                                access: Option[Map[_, _]],
                                attributes: Option[Map[_, _]],
                                clientRoles: Option[Map[_, _]],
                                id: Option[String],
                                name: Option[String],
                                path: Option[String],
                                realmRoles: Option[List[String]],
                                subGroups: Option[List[Group]]
                              )

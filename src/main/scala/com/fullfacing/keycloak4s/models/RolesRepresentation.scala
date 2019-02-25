package models

case class RolesRepresentation(
                                client: Option[Map[_, _]],
                                realm: Option[List[RolesRepresentation]]
                              )

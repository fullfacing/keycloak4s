package models

case class RoleComposites(
                           client: Option[Map[_, _]],
                           realm: Option[List[String]]
                         )

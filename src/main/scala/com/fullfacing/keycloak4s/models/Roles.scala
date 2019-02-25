package models

case class Roles(
                  client: Option[Map[_, _]],
                  realm: Option[Role]
                )

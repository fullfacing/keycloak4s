package models

case class Scope(
                  displayName: Option[String],
                  iconUri: Option[String],
                  id: Option[String],
                  name: Option[String],
                  policies: Option[Policy],
                  resources: Option[Resource]
                )
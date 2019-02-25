package models

case class Resource(
                     id: Option[String],
                     attributes: Option[Map[_, _]],
                     displayName: Option[String],
                     icon_uri: Option[String],
                     name: Option[String],
                     ownerManagedAccess: Option[Boolean],
                     scopes: Option[List[Scope]],
                     `type`: Option[String],
                     uris: Option[List[String]]
                   )

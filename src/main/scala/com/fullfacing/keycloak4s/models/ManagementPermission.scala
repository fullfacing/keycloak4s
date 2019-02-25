package models

case class ManagementPermission(
                                 enabled: Option[Boolean],
                                 resource: Option[String],
                                 scopePermissions: Option[Map[_, _]]
                               )

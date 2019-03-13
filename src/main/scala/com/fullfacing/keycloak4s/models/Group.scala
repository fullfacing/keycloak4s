package com.fullfacing.keycloak4s.models

case class Group(access: Option[Map[String, Boolean]],
                 attributes: Option[Map[String, Seq[String]]],
                 clientRoles: Option[Map[String, Seq[String]]],
                 id: Option[String],
                 name: Option[String],
                 path: Option[String],
                 realmRoles: Option[List[String]],
                 subGroups: Option[List[Group]])

package com.fullfacing.keycloak4s.models

final case class Group(access: Option[Map[String, Boolean]] = None,
                       attributes: Option[Map[String, Seq[String]]] = None,
                       clientRoles: Option[Map[String, Seq[String]]] = None,
                       id: Option[String] = None,
                       name: Option[String] = None,
                       path: Option[String] = None,
                       realmRoles: Option[List[String]] = None,
                       subGroups: Option[List[Group]] = None)

package com.fullfacing.keycloak4s.core.models

final case class Scope(displayName: Option[String],
                       iconUri: Option[String],
                       id: Option[String],
                       name: Option[String],
                       policies: Option[List[Policy]],
                       resources: Option[List[Resource]])

object Scope {
  final case class Create(name: String,
                          displayName: Option[String] = None,
                          iconUri: Option[String] = None,
                          policies: Option[List[Policy]] = None,
                          resources: Option[List[Resource.Create]] = None)
}
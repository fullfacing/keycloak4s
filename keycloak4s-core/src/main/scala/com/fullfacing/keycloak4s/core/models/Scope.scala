package com.fullfacing.keycloak4s.core.models

final case class Scope(displayName: Option[String],
                       iconUri: Option[String],
                       id: Option[String],
                       name: Option[String],
                       policies: Option[Policy],
                       resources: Option[Resource])
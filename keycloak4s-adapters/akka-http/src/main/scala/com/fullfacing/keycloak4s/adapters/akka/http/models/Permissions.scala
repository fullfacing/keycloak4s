package com.fullfacing.keycloak4s.adapters.akka.http.models

final case class ResourceMethods(roles: List[String])

final case class Permissions(access: Map[String, ResourceMethods])
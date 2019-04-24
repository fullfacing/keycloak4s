package com.fullfacing.keycloak4s.adapters.akka.http.models

/** Case class used to extract permissions out of validated access token */
final case class Permissions(resources: Map[String, ResourceRoles])

final case class ResourceRoles(roles: List[String])


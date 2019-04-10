package com.fullfacing.keycloak4s.adapters.akka.http.apollo

final case class Permissions(roles: List[String] = List.empty[String],
                             scopes: List[String] = List.empty[String])
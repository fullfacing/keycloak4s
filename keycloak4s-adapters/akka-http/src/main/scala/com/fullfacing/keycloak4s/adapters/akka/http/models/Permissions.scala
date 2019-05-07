package com.fullfacing.keycloak4s.adapters.akka.http.models

import com.nimbusds.jwt.SignedJWT

/** Case class used to extract permissions out of validated access token */
final case class Permissions(resources: Map[String, ResourceRoles],
                             idToken: Option[SignedJWT] = None)

final case class ResourceRoles(roles: List[String])


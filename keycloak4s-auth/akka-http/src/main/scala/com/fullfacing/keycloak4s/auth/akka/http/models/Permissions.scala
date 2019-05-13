package com.fullfacing.keycloak4s.auth.akka.http.models

import com.nimbusds.jose.Payload
import com.nimbusds.jwt.SignedJWT

/** Case class used to extract permissions out of validated access token */
final case class Permissions(payload: Payload,
                             resources: Map[String, ResourceRoles] = Map.empty[String, ResourceRoles],
                             idToken: Option[SignedJWT] = None)

final case class ResourceRoles(roles: List[String])


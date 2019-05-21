package com.fullfacing.keycloak4s.auth.akka.http.models

import com.nimbusds.jose.Payload

/** Case class used to extract permissions out of validated access token */
final case class AuthPayload(accessToken: Payload,
                             idToken: Option[Payload] = None,
                             resourceRoles: Map[String, ResourceRoles] = Map.empty[String, ResourceRoles])

final case class ResourceRoles(roles: List[String])


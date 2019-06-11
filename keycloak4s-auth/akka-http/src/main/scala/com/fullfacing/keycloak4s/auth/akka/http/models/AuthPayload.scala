package com.fullfacing.keycloak4s.auth.akka.http.models

import com.nimbusds.jose.Payload

/** Contains a parsed and validated access token and ID token (optionally). */
final case class AuthPayload(accessToken: Payload,
                             idToken: Option[Payload] = None)

final case class AuthRoles(roles: List[String])


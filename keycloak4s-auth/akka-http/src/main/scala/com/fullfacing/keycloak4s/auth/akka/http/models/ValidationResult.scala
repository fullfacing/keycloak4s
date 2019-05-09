package com.fullfacing.keycloak4s.auth.akka.http.models

import com.nimbusds.jose.Payload
import com.nimbusds.jwt.SignedJWT

case class ValidationResult(tokenPayload: Payload,
                            idToken: Option[SignedJWT])

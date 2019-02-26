package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.SttpClient

class AttackDetection {

  private val clearLoginFailuresPath = "/attack-detection/brute-force/users"

  def clearLoginFailures(realm: String): Either[ErrorPayload, Unit] = {
    SttpClient.delete()
  }
}

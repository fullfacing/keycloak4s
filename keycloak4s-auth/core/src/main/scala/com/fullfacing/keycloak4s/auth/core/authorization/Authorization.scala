package com.fullfacing.keycloak4s.auth.core.authorization

import java.util.UUID

import com.fullfacing.keycloak4s.auth.core.models.common.PolicyEnforcement

/**
 * @tparam A Type containing params used in implementation of the auth function.
 */
trait Authorization[A] extends PolicyEnforcement {

  val service: String

  /** Abstract function to handle authorization of a request. */
  def authorizeRequest(request: A)(implicit cId: UUID): Boolean
}
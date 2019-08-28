package com.fullfacing.keycloak4s.auth.core.authorization

import java.util.UUID

import com.fullfacing.keycloak4s.auth.core.models.common.PolicyEnforcement

trait Authorization extends PolicyEnforcement {

  val service: String

  /**
   * Abstract function to handle authorization of a request.
   *
   * @param requestPath The path of the HTTP request.
   * @param method      The HTTP method of the request.
   * @param userRoles   The permissions of the user.
   */
  def authorizeRequest(requestPath: String, method: String, userRoles: List[String])(implicit cId: UUID): Boolean
}
package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.Method

/**
 * Object containing roles required for access to the parent class' resource using the specified method.
 * A user will need at least one role from each inner list to be granted access.
 * E.g
 * roles = [ ["resource1-read", "resource1-write"], ["resource2-read", "resource2-write"] ]
 * would translate to (("resource1-read" || "resource1-write") && ("resource2-read", "resource2-write"))
 *
 * @param method The HTTP method these roles apply to. The wildcard method can be used to make this apply to any HTTP method.
 * @param roles  The roles required by the user to be granted access.
 */
case class PathMethodRoles(method: Method,
                           roles: List[List[String]]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.forall(r => r.intersect(userRoles).nonEmpty)
  }
}
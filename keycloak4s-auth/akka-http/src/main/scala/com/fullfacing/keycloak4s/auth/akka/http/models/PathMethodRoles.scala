package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.Method
import com.fullfacing.keycloak4s.auth.akka.http.models.PathMethodRoles.ResourceRoles

/**
 * Object containing roles required for access to be granted to the request path using the specified HTTP method.
 *
 * Each ResourceRoles object (just a List[String]) represents the required roles for one resource in
 * the path. A user would need at least one role from that list to be granted access to the resource,
 * and needs access to all resources in the path for the request to be accepted.
 * E.g
 * roles = [ ["resource1-read", "resource1-write"], ["resource2-read", "resource2-write"] ]
 * would translate to (("resource1-read" || "resource1-write") && ("resource2-read", "resource2-write"))
 *
 * @param method The HTTP method these roles apply to. The wildcard method can be used to make this apply to any HTTP method.
 * @param roles  The roles required by the user to be granted access.
 */
case class PathMethodRoles(method: Method,
                           roles: List[ResourceRoles]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.forall(_.intersect(userRoles).nonEmpty)
  }
}

object PathMethodRoles {
  type ResourceRoles = List[String]
}
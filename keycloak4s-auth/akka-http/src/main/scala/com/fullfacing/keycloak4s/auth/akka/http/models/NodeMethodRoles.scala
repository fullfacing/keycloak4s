package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.Method

case class NodeMethodRoles(method: Method,
                           roles: List[String]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.intersect(userRoles).nonEmpty
  }
}

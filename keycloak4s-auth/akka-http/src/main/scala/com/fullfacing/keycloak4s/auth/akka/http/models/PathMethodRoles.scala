package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.Method

case class PathMethodRoles(method: Method,
                           roles: List[List[String]]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.forall(r => r.intersect(userRoles).nonEmpty)
  }
}
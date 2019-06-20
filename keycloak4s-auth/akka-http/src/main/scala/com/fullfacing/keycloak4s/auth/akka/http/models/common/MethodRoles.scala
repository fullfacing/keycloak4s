package com.fullfacing.keycloak4s.auth.akka.http.models.common

import com.fullfacing.keycloak4s.core.models.enums.Method

case class MethodRoles(method: Method,
                       roles: List[String]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.intersect(userRoles).nonEmpty
  }
}

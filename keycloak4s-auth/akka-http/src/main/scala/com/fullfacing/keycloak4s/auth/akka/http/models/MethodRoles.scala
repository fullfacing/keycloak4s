package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.Method

class MethodRoles(val method: Method,
                  val roles: List[String]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.intersect(userRoles).nonEmpty
  }
}

object MethodRoles {
  def apply(method: Method, roles: List[String]): MethodRoles = new MethodRoles(method, roles)
}
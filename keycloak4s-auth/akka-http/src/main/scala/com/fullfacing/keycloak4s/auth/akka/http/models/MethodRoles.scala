package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.HttpMethod

class MethodRoles(val method: HttpMethod,
                  val roles: List[String]) {

  def evaluateUserAccess(userRoles: List[String]): Boolean = {
    roles.intersect(userRoles).nonEmpty
  }
}

object MethodRoles {
  def apply(method: HttpMethod, roles: List[String]): MethodRoles = new MethodRoles(method, roles)
}
package com.fullfacing.keycloak4s.auth.akka.http.models.path

import com.fullfacing.keycloak4s.core.models.enums.Method
import org.json4s.JsonAST.JObject

/**
 * Object containing roles required for access to be granted to the request path using the specified HTTP method.
 *
 * @param method The HTTP method these roles apply to. The wildcard method can be used to make this apply to any HTTP method.
 * @param roles  The roles required by the user to be granted access.
 */

final case class PathMethodRoles(method: Method,
                                 roles: AndOr) {

  private def eval(e: Either[AndOr, String], userRoles: List[String]): Boolean = e match {
    case Right(s)  => userRoles.contains(s)
    case Left(obj) => evaluateUserAccess(obj, userRoles)
  }

  def evaluateUserAccess(configRoles: AndOr = roles, userRoles: List[String]): Boolean = configRoles match {
    case And(eithers) => eithers.forall(eval(_, userRoles))
    case Or(eithers)  => eithers.exists(eval(_, userRoles))
  }
}

object PathMethodRoles {

  final case class Create(method: Method,
                          roles: JObject)

  def apply(methodRoles: Create): PathMethodRoles = {
    PathMethodRoles(methodRoles.method, AndOr(methodRoles.roles))
  }
}
package com.fullfacing.keycloak4s.auth.akka.http.models.path

import com.fullfacing.keycloak4s.auth.akka.http.Logging
import com.fullfacing.keycloak4s.core.models.enums.Method
import org.json4s.JsonAST.{JArray, JObject, JString, JValue}

/**
 * Object containing roles required for access to be granted to the request path using the specified HTTP method.
 *
 * @param method The HTTP method these roles apply to. The wildcard method can be used to make this apply to any HTTP method.
 * @param roles  The roles required by the user to be granted access.
 */

final case class PathMethodRoles(method: Method,
                                 roles: RequiredRoles) {

  private def eval(e: Either[RequiredRoles, String], userRoles: List[String]): Boolean = e match {
    case Right(s)  => userRoles.contains(s)
    case Left(obj) => evaluateUserAccess(obj, userRoles)
  }

  def evaluateUserAccess(configRoles: RequiredRoles = roles, userRoles: List[String]): Boolean = configRoles match {
    case And(eithers) => eithers.forall(eval(_, userRoles))
    case Or(eithers)  => eithers.exists(eval(_, userRoles))
  }
}

object PathMethodRoles {

  final case class Create(method: Method,
                          roles: JValue)

  def apply(methodRoles: Create): PathMethodRoles = methodRoles.roles match {
    case andOr: JObject =>
      PathMethodRoles(methodRoles.method, RequiredRoles(andOr))

    case JString(s) =>
      PathMethodRoles(methodRoles.method, And(List(Right(s))))

    case JArray(arr) =>
      val roles = arr.collect { case string: JString => Right(string.s)}
      PathMethodRoles(methodRoles.method, Or(roles))

    case _ =>
      Logging.authConfigInitException()
  }
}
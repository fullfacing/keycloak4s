package com.fullfacing.keycloak4s.auth.akka.http.models.node

import akka.http.scaladsl.model.HttpMethod
import com.fullfacing.keycloak4s.auth.akka.http.models.common.MethodRoles
import com.fullfacing.keycloak4s.auth.akka.http.models.{Continue, Evaluation, Result}
import com.fullfacing.keycloak4s.core.models.enums.{Methods, PolicyEnforcementMode, PolicyEnforcementModes}

/**
 * Policy configuration for a particular path segment, as well as all its sub paths.
 *
 * @param segment       Path of this node.
 * @param roles         Required permissions for this resource based on the HTTP method.
 * @param nodes         The next configured path segments.
 */
case class ResourceNode(segment: String,
                        roles: List[MethodRoles],
                        nodes: List[ResourceNode] = List.empty[ResourceNode],
                        enforcementMode: PolicyEnforcementMode = PolicyEnforcementModes.Enforcing) extends Node {

  /**
   * Looks for a configured MethodRole that can apply to all HTTP Methods of the request,
   * and checks if the user has any of those roles.
   */
  def evaluateWildcardMethodsRole(userRoles: List[String]): Boolean = {
    roles.find(_.method == Methods.All).exists(_.evaluateUserAccess(userRoles))
  }

  /**
   * Checks if user has access to this resource using the HTTP method.
   *
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   * @return          The final result or a signal to continue evaluation.
   */
  def evaluate(method: HttpMethod, userRoles: List[String]): Evaluation[ResourceNode] = {
    if (evaluateWildcardMethodsRole(userRoles)) {
      Continue(this)
    } else {
      //Check if there is a method rule config defined for this node matching the HTTP method of the request
      roles.find(_.method.value == method.value) match {
        case None    => Result(noMatchingPolicy())
        //Check if user has the role for this method on this resource
        case Some(m) => if (m.evaluateUserAccess(userRoles)) Continue(this) else Result(false)
      }
    }
  }
}
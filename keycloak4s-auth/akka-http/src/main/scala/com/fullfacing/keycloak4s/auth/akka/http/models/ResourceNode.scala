package com.fullfacing.keycloak4s.auth.akka.http.models

import akka.http.scaladsl.model.HttpMethod
import com.fullfacing.keycloak4s.core.models.enums.{Methods, PolicyEnforcementMode, PolicyEnforcementModes}

/**
 * Policy configuration for a particular path segment, as well as all its sub paths.
 *
 * @param resource      Path of this node.
 * @param roles         Required permissions for this resource based on the HTTP method.
 * @param nodes         The next configured path segments.
 */
case class ResourceNode(resource: String,
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
   * Checks if user has access to this resource.
   * A none is returned when there is no final authorisation result at this stage i.e. authorisation of this
   * resource was successful and evaluation can continue down the request path.
   *
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   * @return          Final result (Some(result)) or signal to continue evaluation down the request path (None).
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
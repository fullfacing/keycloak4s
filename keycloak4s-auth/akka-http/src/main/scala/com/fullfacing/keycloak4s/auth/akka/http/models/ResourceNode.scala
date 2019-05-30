package com.fullfacing.keycloak4s.auth.akka.http.models

import akka.http.scaladsl.model.HttpMethod
import com.fullfacing.keycloak4s.core.models.enums.Methods

/**
 *
 *
 * @param resource      Path of this node.
 * @param roles         Required permissions for this resource based on the HTTP method.
 * @param nodes         The next configured path segments.
 */
class ResourceNode(val resource: String,
                   val roles: List[MethodRoles],
                   val nodes: List[ResourceNode] = List.empty[ResourceNode]) {

  def evaluateWildcardRole(method: HttpMethod, userRoles: List[String]): Boolean = {
    nodes.find(_.resource == "*").exists { node =>
      node.roles.find(_.method.value == method.value)
        .exists(_.evaluateUserAccess(userRoles))
    }
  }

  /**
   * Looks for a configured MethodRole that can apply to all HTTP Methods of the request,
   * and checks if the user ahs any of those roles.
   */
  def evaluateWildcardMethodsRole(userRoles: List[String]): Boolean = {
    roles.find(_.method == Methods.All).exists(_.evaluateUserAccess(userRoles))
  }

  /**
   * Checks if user has access to this resource.
   * A none is returned there is no final authorisation result at this stage i.e. authorisation of this
   * resource was successful and evaluation can continue down the request path.
   *
   * @param sec       The security configuration object.
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   * @return          Final result (Some(result)) or signal to continue evaluation down the request path (None).
   */
  def evaluate(sec: SecurityConfig, method: HttpMethod, userRoles: List[String]): Option[Boolean] = {
    if (evaluateWildcardRole(method, userRoles)) {
      Some(true)
    } else if (evaluateWildcardMethodsRole(userRoles)) {
      None
    } else {
      //Check if there is a method rule config defined for this node matching the HTTP method of the request
      roles.find(_.method.value == method.value) match {
        case None    => Some(sec.noMatchingPolicy())
        //Check if user has the role for this method on this resource
        case Some(m) => if (m.evaluateUserAccess(userRoles)) None else Some(false)
      }
    }
  }
}

object ResourceNode {
  def apply(resource: String,
            roles: List[MethodRoles],
            wildcardRole: Option[String],
            nodes: List[ResourceNode] = List.empty[ResourceNode]): ResourceNode =
    new ResourceNode(resource, roles, nodes)
}
package com.fullfacing.keycloak4s.auth.akka.http.models

import akka.http.scaladsl.model.{HttpMethod => AkkaHttpMethod}

/**
 *
 * @param name          Name of the resource as would appear in the user's auth token. Path segment is used if set to None.
 * @param resource      Path of this node.
 * @param roles         Required permissions for this resource based on the HTTP method.
 * @param wildcardRole  Role required to stop evaluation further down the path and authorise the request.
 * @param nodes         The next configured path segments.
 */
class ResourceNode(val name: Option[String],
                   val resource: String,
                   val roles: List[MethodRoles],
                   val wildcardRole: Option[String],
                   val nodes: List[ResourceNode] = List.empty[ResourceNode]) {

  /**
   * Checks if user has access to this resource.
   * A none is returned there is no final authorisation result at this stage i.e. authorisation of this
   * resource was successful and evaluation can continue down the request path.
   *
   * @param sec       The security configuration object.
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   * @return
   */
  def evaluate(sec: SecurityConfig, method: AkkaHttpMethod, userRoles: List[String]): Option[Boolean] = {
    if (wildcardRole.exists(userRoles.contains)) {
      Some(true)
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
  def apply(name: Option[String],
            resource: String,
            roles: List[MethodRoles],
            wildcardRole: Option[String],
            nodes: List[ResourceNode] = List.empty[ResourceNode]): ResourceNode =
    new ResourceNode(name, resource, roles, wildcardRole, nodes)
}
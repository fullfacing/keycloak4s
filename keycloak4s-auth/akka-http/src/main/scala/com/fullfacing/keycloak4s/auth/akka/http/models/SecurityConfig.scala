package com.fullfacing.keycloak4s.auth.akka.http.models

import akka.http.scaladsl.model.{HttpMethod => AkkaHttpMethod}
import com.fullfacing.keycloak4s.core.models.enums.{PolicyEnforcementMode, PolicyEnforcementModes}

/**
 * Configuration object containing the policies for managing access to the server.
 *
 * @param service         Name of the api/microservice being secured.
 * @param enforcementMode Determines how requests with no matching policies are handled.
 * @param adminRoles      Optional roles that would automatically accept a user request, regardless of the request path.
 * @param nodes           The configured and secured resource segments on the server.
 */
class SecurityConfig(val service: String,
                     val enforcementMode: PolicyEnforcementMode,
                     val adminRoles: List[MethodRoles],
                     val nodes: List[ResourceNode]) {

  /**
   * Checks if the user has admin access to the server, or if the enforcement mode is set to disabled,
   * both of which would remove the need to continue evaluation at a resource level and will allow the
   * request to proceed.
   *
   * @param method     HTTP method of the request.
   * @param userRoles  The user's permissions
   */
  def evaluate(method: AkkaHttpMethod, userRoles: List[String]): Boolean = {
    lazy val isAdmin = adminRoles.find(_.method.value == method.value)
      .exists(_.evaluateUserAccess(userRoles))

    enforcementMode == PolicyEnforcementModes.Disabled || isAdmin
  }

  def noMatchingPolicy(): Boolean = enforcementMode match {
    case PolicyEnforcementModes.Enforcing  => false
    case PolicyEnforcementModes.Permissive => true
    case PolicyEnforcementModes.Disabled   => true
  }
}

object SecurityConfig {

  def apply(config: String): SecurityConfig = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read
    read[SecurityConfig](config)
  }

  def apply(service: String,
            enforcementMode: PolicyEnforcementMode,
            adminRoles: List[MethodRoles],
            nodes: List[ResourceNode]): SecurityConfig =
    new SecurityConfig(service, enforcementMode, adminRoles, nodes)
}
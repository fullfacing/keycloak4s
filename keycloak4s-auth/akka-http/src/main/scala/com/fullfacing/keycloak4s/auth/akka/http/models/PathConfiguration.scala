package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.{PolicyEnforcementMode, PolicyEnforcementModes}

/**
 * Security configuration for a top level authorisation directive.
 *
 * Example usages:
 * {
 *  "path": "*",
 *  "roles": [
 *    {
 *      "method": "*",
 *      "roles": [["admin"]]
 *    }
 *  ]
 * }
 *
 * {
 *  "path": "v1/resource1/resource2",
 *  "roles": [
 *   {
 *      "method": "*",
 *      "roles": [["admin"]]
 *    },
 *    {
 *      "method": "GET",
 *      "roles": [["resource1-read", "resource1-write"], ["resource2-read", "resource2-write"]]
 *    },
 *    {
 *      "method": "POST",
 *      "roles": [["resource1-write"], ["resource2-write"]]
 *    }
 *  ]
 * }
 *
 * @param service         Name of the server being secured.
 * @param enforcementMode Determines how requests with no matching sec policy are handled.
 * @param paths           The configured policies.
 */
class PathConfiguration(val service: String,
                        val enforcementMode: PolicyEnforcementMode,
                        val paths: List[PathRoles]) {

  /** Determines what to do when there is no matching policy for the request */
  def noMatchingPolicy(): Boolean = enforcementMode match {
    case PolicyEnforcementModes.Enforcing  => false
    case PolicyEnforcementModes.Permissive => true
    case PolicyEnforcementModes.Disabled   => true
  }

  def policyDisabled(): Boolean = {
    enforcementMode == PolicyEnforcementModes.Disabled
  }
}

object PathConfiguration {
  import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
  import org.json4s.jackson.Serialization.read

  case class Create(service: String,
                    enforcementMode: PolicyEnforcementMode,
                    paths: List[PathRoles.Create])

  def apply(config: Create): PathConfiguration =
    new PathConfiguration(config.service, config.enforcementMode, config.paths.map(PathRoles(_)))

  def apply(config: String): PathConfiguration = {
    apply(read[Create](config))
  }
}

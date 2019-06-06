package com.fullfacing.keycloak4s.auth.akka.http.models

import com.fullfacing.keycloak4s.core.models.enums.{PolicyEnforcementMode, PolicyEnforcementModes}

/**
 * Configuration object containing the policies for managing access to the server.
 *
 * @param service         Name of the api/microservice being secured.
 * @param enforcementMode Determines how requests with no matching policies are handled.
 * @param nodes           The configured and secured resource segments on the server.
 */
class NodeConfiguration(val service: String,
                        val nodes: List[ResourceNode],
                        val enforcementMode: PolicyEnforcementMode = PolicyEnforcementModes.Enforcing) extends Node

object NodeConfiguration {

  def apply(config: String): NodeConfiguration = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read
    read[NodeConfiguration](config)
  }

  def apply(service: String,
            nodes: List[ResourceNode],
            enforcementMode: PolicyEnforcementMode = PolicyEnforcementModes.Enforcing): NodeConfiguration =
    new NodeConfiguration(service, nodes, enforcementMode)
}
package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.models.{Continue, Node, ResourceNode, Result}
import com.fullfacing.keycloak4s.core.models.enums.{PolicyEnforcementMode, PolicyEnforcementModes}

import scala.annotation.tailrec

/**
 * Configuration object containing the policies for managing access to the server.
 *
 * @param service         Name of the api/microservice being secured.
 * @param enforcementMode Determines how requests with no matching policies are handled.
 * @param nodes           The configured and secured resource segments on the server.
 */
class NodeAuthorisation(val service: String,
                        val nodes: List[ResourceNode],
                        val enforcementMode: PolicyEnforcementMode = PolicyEnforcementModes.Enforcing) extends Authorisation with Node {

  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   *
   * @param path      The path of the HTTP request.
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   */
  def authoriseRequest(path: Path, method: HttpMethod, userRoles: List[String]): Boolean = {
    @tailrec
    def loop(path: List[String], node: Node): Boolean = path match {
      case Nil    => true
      case h :: t =>
        node.evaluateSecurityPolicy(h, method, userRoles) match {
          case Result(result) => result
          case Continue(n)    => loop(t, n)
        }
    }

    lazy val listPath = extractResourcesFromPath(path)
    listPath.nonEmpty && loop(listPath, this)
  }
}

object NodeAuthorisation {

  def apply(config: String): NodeAuthorisation = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read
    read[NodeAuthorisation](config)
  }

  def apply(service: String,
            nodes: List[ResourceNode],
            enforcementMode: PolicyEnforcementMode = PolicyEnforcementModes.Enforcing): NodeAuthorisation =
    new NodeAuthorisation(service, nodes, enforcementMode)
}
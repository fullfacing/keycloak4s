package com.fullfacing.keycloak4s.adapters.akka.http.services

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.{Directive0, Directive1}
import akka.http.scaladsl.server.Directives.{authorize, extractMethod, extractUnmatchedPath, pass, provide}
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives.{checkPermissions, scopeMap}
import com.fullfacing.keycloak4s.adapters.akka.http.models.{Permissions, ResourceNode, ResourceRoles}

import scala.util.matching.Regex

object Authorisation {

  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  /**
   * Top level authorisation.
   * Authorises user's access to the service and returns only permissions specific to the service.
   *
   * @param permissions     All permissions in user's token.
   * @param resourceServer  Name of the resource-server.
   * @return                Permissions specific to given resource server.
   */
  def authoriseResourceServerAccess(permissions: Permissions, resourceServer: String): Directive1[Permissions] = {
    val f = { r: ResourceRoles =>
      val rr = r.roles.map(_.split("-")).groupBy(_.headOption).collect { case (Some(k), v) =>
        k -> ResourceRoles(v.flatMap(_.lastOption))
      }
      Permissions(rr)
    }

    checkPermissions(resourceServer, permissions, r => provide(f(r)))
  }

  /** Extracts the path and http method and authorises the request */
  def authoriseRequest(nodes: List[ResourceNode], permissions: Permissions): Directive0 = {
    extractUnmatchedPath.flatMap { path =>
      extractMethod.flatMap { method =>
        evaluate(nodes, permissions, path, method)
      }
    }
  }

  /**
   * Loops through the recursive Path object and compares each segment in the path to the nodes config
   * object to determine which segments refer to secure resources that need to be authorised.
   * The user's access to each secured resource in the path is evaluated.
   *
   * @param nodes       A config object containing all secured paths.
   * @param permissions The user's permissions to access the service.
   * @param path        The path of the request.
   * @param method      The HTTP method of the request.
   */
  private def evaluate(nodes: List[ResourceNode], permissions: Permissions, path: Path, method: HttpMethod): Directive0 = {

    def loop(p: Path, nodes: List[ResourceNode]): Directive0 = if (p.isEmpty) pass else {
      val head = p.head.toString
      //Ignore segments that do not refer to resources
      if (p.startsWithSegment && validUuid.unapplySeq(head).isEmpty) {
        nodes.find(_.resource == head) match {
          case Some(node) =>
            checkPermissions(head, permissions, r => authoriseMethod(r, method))
              .tflatMap(_ => loop(p.tail, node.nodes))
          case None       =>
            loop(p.tail, nodes)
        }
      } else {
        loop(p.tail, nodes)
      }
    }

    loop(path, nodes)
  }

  /** Authorises the operation based on the HTTP method and the authorised roles the user has on the resource */
  private def authoriseMethod(resource: ResourceRoles, method: HttpMethod): Directive0 = {
    authorize {
      resource.roles.intersect(scopeMap(method)).nonEmpty
    }
  }
}

package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import java.util.UUID

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.models.common.{AuthResource, MethodRoles}
import com.fullfacing.keycloak4s.auth.akka.http.models.path.{PathMethodRoles, PathRoles}
import com.fullfacing.keycloak4s.core.models.enums.{Methods, PolicyEnforcementMode}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization.read

import scala.annotation.tailrec

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
case class PathAuthorisation(service: String,
                             enforcementMode: PolicyEnforcementMode,
                             paths: List[PathRoles]) extends Authorisation {

  /**
   * Runs through the relevant segments of the request path and collects all rules that apply to the request.
   *
   * @param reqPath  The relevant segments in the request path.
   * @param cfgPaths The configured secure paths of the server.
   * @param d        The segment number of the path, used to compare the request path to the configured paths.
   * @param acc      Accumulated configured wildcard paths that can authorise the path at a higher level.
   */
  @tailrec
  private def findMatchingPaths(reqPath: List[String], cfgPaths: List[PathRoles], d: Int = 0, acc: List[PathRoles] = List.empty): List[PathRoles] = reqPath match {
    //Return the accumulated wildcard paths, as well as the remaining path that matches the request
    case Nil    => acc ++ cfgPaths.filter(_.path.drop(d).isEmpty)
    case h :: t =>
      //Look for a configured wildcard path at the current segment
      val wildcard = acc ++ cfgPaths.find(_.path.drop(d).headOption.contains("*")).toList

      //Filter out the configured paths that do not match the request path at this segment
      val matched  = cfgPaths.filter { cfgPath =>
        cfgPath.path.drop(d).headOption.contains(h)
      }

      //If there is not matching configured path, the accumulated wildcard paths are returned,
      //else the evaluation continues with the rest of the request path and the remaining matching configured paths.
      if (matched.nonEmpty) findMatchingPaths(t, matched, d + 1, wildcard) else wildcard
  }


  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   *
   * @param path      The path of the HTTP request.
   * @param method    The HTTP method of the request.
   * @param userRoles The permissions of the user.
   */
  def authoriseRequest(path: Path, method: HttpMethod, userRoles: List[String])(implicit cId: UUID): Boolean = {
    val matchedPaths = findMatchingPaths(extractSegmentsFromPath(path), paths)

    matchedPaths.exists { p =>

      lazy val hasWildCardRole = p.roles
        .find(_.method == Methods.All)
        .exists(_.evaluateUserAccess(userRoles))

      val hasMethodRole = p.roles.find(_.method.value == method.value) match {
        case None    => noMatchingPolicy()
        case Some(r) => r.evaluateUserAccess(userRoles)
      }

      hasMethodRole || hasWildCardRole
    }
  }
}


object PathAuthorisation {

  case class Create(service: String,
                    enforcementMode: PolicyEnforcementMode,
                    paths: List[PathRoles.Create],
                    resources: List[AuthResource])

  def apply(config: Create): PathAuthorisation =
    new PathAuthorisation(config.service, config.enforcementMode, config.paths.map(PathRoles(_)))

  def apply(config: String): PathAuthorisation = {
    val create = read[Create](config)

    val pathRoles = create.paths.map { pathConfig =>
      val roles = pathConfig.path.split("/")
        .filter(seg => seg.startsWith("{{") && seg.endsWith("}}"))
        .flatMap { segment =>
          val r = segment.drop(2).dropRight(2)
          create.resources.find(_.resource == r) match {
            case Some(s) => Some(s.auth)
            case _       =>
              Logging.authResourceNotFound(r)
              None
          }
        }.toList

      pathConfig.copy(
        roles = merge(roles.flatten),
        path  = pathConfig.path.replace("{{", "").replace("}}", "")
      )
    }

    PathAuthorisation(
      service         = create.service,
      enforcementMode = create.enforcementMode,
      paths           = pathRoles.map(PathRoles(_))
    )
  }

  def merge(roles: List[MethodRoles]): List[PathMethodRoles] = {
    roles.groupBy(_.method).map { case (m, r) =>
      PathMethodRoles(m, r.map(_.roles))
    }.toList
  }
}

package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.models.{PathConfiguration, PathRoles}
import com.fullfacing.keycloak4s.core.models.enums.Methods

import scala.annotation.tailrec

object PathAuthorisation {

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
   * @param config    The security configuration of the server.
   * @param userRoles The permissions of the user.
   */
  def authoriseRequest(path: Path, method: HttpMethod, config: PathConfiguration, userRoles: List[String]): Boolean = {
    val matchedPaths = findMatchingPaths(Utilities.extractResourcesFromPath(path), config.paths)

    matchedPaths.exists { p =>

      lazy val hasWildCardRole = p.roles
        .find(_.method == Methods.All)
        .exists(_.evaluateUserAccess(userRoles))

      val hasMethodRole = p.roles.find(_.method.value == method.value) match {
        case None    => config.noMatchingPolicy()
        case Some(r) => r.evaluateUserAccess(userRoles)
      }

      hasMethodRole || hasWildCardRole
    }
  }
}

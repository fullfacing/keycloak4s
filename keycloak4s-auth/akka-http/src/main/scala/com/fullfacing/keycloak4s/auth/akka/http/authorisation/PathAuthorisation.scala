package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.models.{PathConfiguration, PathRoles}
import com.fullfacing.keycloak4s.core.models.enums.Methods

import scala.annotation.tailrec

object PathAuthorisation extends Authorisation[PathConfiguration] {

  /**
   * Runs through the request path and collects all rules that apply to the request.
   */
  @tailrec
  private def findMatchingPaths(reqPath: List[String], cfgPaths: List[PathRoles], d: Int = 0, acc: List[PathRoles] = List.empty): List[PathRoles] = reqPath match {
    case Nil    => acc ++ cfgPaths.filter(_.path.drop(d).isEmpty)
    case h :: t =>
      val wildcard = cfgPaths.find(_.path.drop(d).headOption.contains("*")).toList

      val matched  = cfgPaths.filter { cfgPath =>
        cfgPath.path.drop(d).headOption.contains(h)
      }

      if (matched.nonEmpty) findMatchingPaths(t, matched, d + 1, acc ++ wildcard) else acc ++ wildcard
  }


  def authoriseRequest(path: Path, method: HttpMethod, config: PathConfiguration, userRoles: List[String]): Boolean = {
    val matchedPaths = findMatchingPaths(Utilities.extractResourcesFromPath(path), config.paths)

    matchedPaths.exists { p =>

      val hasWildCardRole = p.roles
        .find(_.method == Methods.All)
        .exists(_.evaluateUserAccess(userRoles))

      lazy val hasMethodRole = p.roles.find(_.method.value == method.value) match {
        case None    => config.noMatchingPolicy()
        case Some(r) => r.evaluateUserAccess(userRoles)
      }

      hasWildCardRole || hasMethodRole
    }
  }
}

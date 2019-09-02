package com.fullfacing.keycloak4s.auth.core.authorization

import java.util.UUID

import cats.implicits._
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.auth.core.models.common.{AuthSegment, MethodRoles}
import com.fullfacing.keycloak4s.auth.core.models.path._
import com.fullfacing.keycloak4s.core.models.enums.{Methods, PolicyEnforcementMode}
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import org.json4s.jackson.Serialization.read
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization._
import scala.annotation.tailrec
import scala.util.matching.Regex

/**
 * Security configuration for a top level authorization directive.
 *
 * @param service         Name of the server being secured.
 * @param enforcementMode Determines how requests with no matching sec policy are handled.
 * @param paths           The configured policies.
 */
final case class PathAuthorization(service: String,
                                   enforcementMode: PolicyEnforcementMode,
                                   paths: List[PathRule]) extends Authorization[AuthRequest] {

  /**
   * Runs through the relevant segments of the request path and collects all rules that apply to the request.
   *
   * @param reqPath  The relevant segments in the request path.
   * @param cfgPaths The configured secure paths of the server.
   * @param d        The segment number of the path, used to compare the request path to the configured paths.
   * @param acc      Accumulated configured wildcard paths that can authorize the path at a higher level.
   */
  @tailrec
  private def findMatchingPaths(reqPath: List[String], cfgPaths: List[PathRule], d: Int = 0, acc: List[PathRule] = List.empty): List[PathRule] = reqPath match {
    //Return the accumulated wildcard paths, as well as the remaining path that matches the request
    case Nil    => acc ++ cfgPaths.filter(_.path.drop(d).isEmpty)
    case h :: t =>
      //Look for a configured wildcard path at the current segment
      val wildcard = acc ++ cfgPaths.find(_.path.drop(d).headOption.contains("*")).toList

      //Filter out the configured paths that do not match the request path at this segment
      val matched  = cfgPaths.filter { cfgPath =>
        cfgPath.path.drop(d).headOption.contains(h)
      }

      //If there is no matching configured path, the accumulated wildcard paths are returned,
      //else the evaluation continues with the rest of the request path and the remaining matching configured paths.
      if (matched.nonEmpty) findMatchingPaths(t, matched, d + 1, wildcard) else wildcard
  }

  private def extractPath(path: String): List[String] =
    path.split("/").toList.collect {
      case s if validUuid.unapplySeq(s).nonEmpty => "{id}"
      case s if s.nonEmpty                       => s
    }

  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   */
  override def authorizeRequest(request: AuthRequest)(implicit cId: UUID): Boolean = {
    val matchedPaths = findMatchingPaths(extractPath(request.path), paths)

    lazy val methodAllowed = matchedPaths.exists { p =>

      lazy val hasWildCardRole = p.methodRoles
        .find(_.method == Methods.All)
        .exists(_.evaluateUserAccess(userRoles = request.userRoles))

      val hasMethodRole = p.methodRoles.find(_.method.value == request.method)
        .exists(_.evaluateUserAccess(userRoles = request.userRoles))

      hasMethodRole || hasWildCardRole
    }

    val result = matchedPaths match {
      case Nil => noMatchingPolicy()
      case _   => methodAllowed
    }

    if (!result) Logging.authorizationPathDenied(cId, request.method, request.path)
    result
  }
}


object PathAuthorization {

  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  final case class Create(service: String,
                          enforcementMode: PolicyEnforcementMode,
                          paths: List[PathRule.Create],
                          segments: List[AuthSegment])

  /**
   * @param path        The path of the HTTP request.
   * @param method      The HTTP method of the request.
   * @param userRoles   The permissions of the user.
   */
  final case class AuthRequest(path: String,
                               method: String,
                               userRoles: List[String])

  /**
   * Apply that converts certain simplifications in the json config object into the required case class
   * structure needed for evaluation.
   */
  def apply(config: String): PathAuthorization = {
    val create = read[Create](config)

    val pathRoles = create.paths.map { pathConfig =>
      PathRule(
        path        = pathConfig.path.replace("{{", "").replace("}}", ""),
        methodRoles = merge(
          findAuthValuesInConfig(pathConfig, create.segments),
          pathConfig.methodRoles.map(PathMethodRoles.apply)
        )
      )
    }

    PathAuthorization(
      service         = create.service,
      enforcementMode = create.enforcementMode,
      paths           = pathRoles
    )
  }

  private def findAuthValuesInConfig(pathConfig: PathRule.Create, savedSegments: List[AuthSegment]): List[MethodRoles] =
    pathConfig.path.split("/")
      .filter(seg => seg.startsWith("{{") && seg.endsWith("}}"))
      .flatMap { segment =>
        val r = segment.drop(2).dropRight(2)
        savedSegments.find(_.segment == r) match {
          case Some(s) => Some(s.methodRoles)
          case _       =>
            Logging.authResourceNotFound(r)
            None
        }
      }.toList.flatten


  private def merge(segmentRoles: List[MethodRoles], pathRoles: List[PathMethodRoles]): List[PathMethodRoles] = {
    Methods.values.toList.flatMap { method =>

      val sr: Option[RequiredRoles] = segmentRoles.filter(_.method == method) match {
        case Nil      => None
        case h :: Nil => Some(Or(h.roles.map(_.asRight)))
        case l        => Some(And(l.map(r => Or(r.roles.map(_.asRight)).asLeft)))
      }

      val pr = pathRoles.find(_.method == method).map(_.roles)

      val roles = (sr, pr) match {
        case (Some(s), Some(p)) => Some(And(List(Left(s), Left(p))))
        case (None, Some(p))    => Some(p)
        case (Some(s), None)    => Some(s)
        case (None, None)       => None
      }

      roles.map(r => PathMethodRoles(method, r))
    }
  }
}
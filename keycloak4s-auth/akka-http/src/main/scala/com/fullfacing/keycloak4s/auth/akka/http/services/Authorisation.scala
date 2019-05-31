package com.fullfacing.keycloak4s.auth.akka.http.services

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpMethod => AkkaHttpMethod}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.auth.akka.http.models._

import scala.annotation.tailrec
import scala.util.matching.Regex

object Authorisation {

  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  def authoriseResourceServerAccess(permissions: AuthPayload, resourceServer: String): Directive1[List[String]] = {
    checkPermissions(resourceServer, permissions, r => provide(r.roles))
  }

  @tailrec
  private def extractResourcesFromPath(path: Path, acc: List[String] = List.empty[String]): List[String] = {
    lazy val isResource = path.startsWithSegment && validUuid.unapplySeq(path.head.toString).isEmpty

    if (path.isEmpty) acc
    else if (isResource) extractResourcesFromPath(path.tail, acc :+ path.head.toString)
    else extractResourcesFromPath(path.tail, acc)
  }

  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   *
   * @param path      The path of the HTTP request.
   * @param method    The HTTP method of the request.
   * @param sec       The security configuration of the server.
   * @param userRoles The permissions of the user.
   */
  def authoriseRequest(path: Path, method: AkkaHttpMethod, sec: SecurityConfig, userRoles: List[String]): Directive0 = {
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
    if (sec.policyDisabled() || (listPath.nonEmpty && loop(listPath, sec))) {
      pass
    } else {
      authorisationFailed()
    }
  }
}

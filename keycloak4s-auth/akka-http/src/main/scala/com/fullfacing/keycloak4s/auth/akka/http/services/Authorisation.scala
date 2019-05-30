package com.fullfacing.keycloak4s.auth.akka.http.services

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpMethod => AkkaHttpMethod}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1}
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.auth.akka.http.models.{AuthPayload, ResourceNode, SecurityConfig}

import scala.annotation.tailrec
import scala.util.matching.Regex

object Authorisation {

  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  def authoriseResourceServerAccess(permissions: AuthPayload, resourceServer: String): Directive1[List[String]] = {
    checkPermissions(resourceServer, permissions, r => provide(r.roles))
  }

  /** Extracts the path and http method and authorises the request */
  def authoriseRequest(conf: SecurityConfig, userRoles: List[String]): Directive0 = {
    extractUnmatchedPath.flatMap { path =>
      extractMethod.flatMap { method =>
        policyConfigEvaluation(path, method, conf, userRoles)
      }
    }
  }

  @tailrec
  def extractResourcesFromPath(path: Path, acc: List[String] = List.empty[String]): List[String] = {
    lazy val isResource = path.startsWithSegment && validUuid.unapplySeq(path.head.toString).isEmpty

    if (path.isEmpty) acc
    else if (isResource) extractResourcesFromPath(path.tail, acc :+ path.head.toString)
    else extractResourcesFromPath(path.tail, acc)
  }


  /**
   * ???
   *
   * @param path      The path of the HTTP request.
   * @param method    The HTTP method of the request.
   * @param sec       The security configuration of the server.
   * @param userRoles The permissions of the user.
   */
  def policyConfigEvaluation(path: Path, method: AkkaHttpMethod, sec: SecurityConfig, userRoles: List[String]): Directive0 = {
    @tailrec
    def loop(path: List[String], nodes: List[ResourceNode]): Boolean = path match {
      case Nil       => true
      case h :: t    =>
        //Find node matching path segment
        nodes.find(_.resource == h) match {
          case None       => sec.noMatchingPolicy()
          case Some(node) =>
            node.evaluate(sec, method, userRoles) match {
              case Some(res) => res
              case None      => loop(t, node.nodes)
            }
        }
    }

    lazy val listPath = extractResourcesFromPath(path)
    if (sec.evaluate(method, userRoles)) {
      pass
    } else if (listPath.nonEmpty && loop(listPath, sec.nodes)) {
      pass
    } else {
      authorisationFailed()
    }
  }
}

package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.models._

import scala.annotation.tailrec

object NodeAuthorisation {

  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   *
   * @param path      The path of the HTTP request.
   * @param method    The HTTP method of the request.
   * @param sec       The security configuration of the server.
   * @param userRoles The permissions of the user.
   */
  def authoriseRequest(path: Path, method: HttpMethod, sec: NodeConfiguration, userRoles: List[String]): Boolean = {
    @tailrec
    def loop(path: List[String], node: Node): Boolean = path match {
      case Nil    => true
      case h :: t =>
        node.evaluateSecurityPolicy(h, method, userRoles) match {
          case Result(result) => result
          case Continue(n)    => loop(t, n)
        }
    }

    lazy val listPath = Utilities.extractResourcesFromPath(path)
    listPath.nonEmpty && loop(listPath, sec)
  }
}

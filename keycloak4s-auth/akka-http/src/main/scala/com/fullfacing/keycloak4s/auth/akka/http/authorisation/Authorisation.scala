package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import java.util.UUID

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, provide}
import akka.http.scaladsl.server.StandardRoute.toDirective
import akka.http.scaladsl.server.util.Tuple.yes
import akka.http.scaladsl.server.{Directive, Directive1, StandardRoute}
import com.fullfacing.keycloak4s.auth.akka.http.Logging
import com.fullfacing.keycloak4s.auth.akka.http.PayloadImplicits._
import com.fullfacing.keycloak4s.auth.akka.http.models.common.PolicyEnforcement
import com.fullfacing.keycloak4s.auth.akka.http.models.{AuthPayload, AuthRoles}
import com.fullfacing.keycloak4s.core.Exceptions.UNAUTHORIZED

import scala.annotation.tailrec
import scala.util.matching.Regex

trait Authorisation extends PolicyEnforcement {

  val service: String
  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  @tailrec
  final def extractSegmentsFromPath(path: Path, acc: List[String] = List.empty[String]): List[String] = {
    lazy val isResource = path.startsWithSegment && validUuid.unapplySeq(path.head.toString).isEmpty

    if (path.isEmpty) acc
    else if (isResource) extractSegmentsFromPath(path.tail, acc :+ path.head.toString)
    else extractSegmentsFromPath(path.tail, acc)
  }

  def authoriseRequest(path: Path, method: HttpMethod, userRoles: List[String])(implicit cId: UUID): Boolean
}

object Authorisation {

  private implicit def routeToDirective[A](route: StandardRoute): Directive[A] =
    toDirective[A](route)(yes[A])

  /**
   * Looks for the requested resource in the user's permissions from the validated access token.
   * The request is rejected if not found.
   *
   * @param resource     The resource the user is attempting to access.
   * @param permissions  The resources and methods allowed for the user.
   * @param success      A directive to create if the user has access to the resource.
   * @return             The resulting directive from the auth result and the function provided.
   */
  def checkPermissions[A](resource: String, permissions: AuthPayload, success: AuthRoles => Directive[A])(implicit cId: UUID): Directive[A] = {
    permissions.accessToken.extractResourceAccess.find { case (k, _) => k.equalsIgnoreCase(resource) } match {
      case Some((_, v)) => success(v)
      case None         => authorisationFailed()
    }
  }

  def authoriseResourceServerAccess(permissions: AuthPayload, resourceServer: String)(implicit cId: UUID): Directive1[List[String]] = {
    checkPermissions(resourceServer, permissions, r => provide(r.roles))
  }

  def authorisationFailed()(implicit cId: UUID): StandardRoute = {
    Logging.authorisationFailed(cId)
    complete(HttpResponse(UNAUTHORIZED.code, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, UNAUTHORIZED.getMessage)))
  }
}

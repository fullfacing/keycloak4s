package com.fullfacing.keycloak4s.auth.akka.http.authorization

import java.util.UUID

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, extractMethod, extractUnmatchedPath, provide}
import akka.http.scaladsl.server.StandardRoute.toDirective
import akka.http.scaladsl.server.util.Tuple.yes
import akka.http.scaladsl.server.{Directive, Directive1, StandardRoute}
import com.fullfacing.keycloak4s.auth.core.PayloadImplicits._
import com.fullfacing.keycloak4s.auth.core.models.common.PolicyEnforcement
import com.fullfacing.keycloak4s.auth.core.models.{AuthPayload, AuthRoles}
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.core.Exceptions.UNAUTHORIZED

import scala.annotation.tailrec
import scala.util.matching.Regex

trait Authorization extends PolicyEnforcement {

  val service: String
  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  @tailrec
  final def extractSegmentsFromPath(path: Path, acc: List[String] = List.empty[String]): List[String] = {
    if (path.isEmpty) {
      acc
    } else if (validUuid.unapplySeq(path.head.toString).nonEmpty) {
      extractSegmentsFromPath(path.tail, acc :+ "{id}")
    } else if (path.startsWithSegment) {
      extractSegmentsFromPath(path.tail, acc :+ path.head.toString)
    } else {
      extractSegmentsFromPath(path.tail, acc)
    }
  }

  def authorizeRequest(path: Path, method: HttpMethod, userRoles: List[String])(implicit cId: UUID): Boolean
}

object Authorization {

  private implicit def routeToDirective[A](route: StandardRoute): Directive[A] =
    toDirective[A](route)(yes[A])

  /**
   * Looks for the requested resource in the user's permissions from the validated access token.
   * The request is rejected if not found.
   *
   * @param segment      The resource/action the user is attempting to access.
   * @param permissions  The resources and methods allowed for the user.
   * @param success      A directive to create if the user has access to the resource.
   * @return             The resulting directive from the auth result and the function provided.
   */
  def checkPermissions[A](segment: String, permissions: AuthPayload, success: AuthRoles => Directive[A])(implicit cId: UUID): Directive[A] = {
    permissions.accessToken.extractResourceAccess.find { case (k, _) => k.equalsIgnoreCase(segment) } match {
      case Some((_, v)) => success(v)
      case None         => Logging.authorizationDenied(cId, segment); authorizationFailed()
    }
  }

  def authorizeResourceServerAccess(permissions: AuthPayload, resourceServer: String)(implicit cId: UUID): Directive1[(Path, HttpMethod, List[String])] = {
    extractMethod.flatMap { method =>
      extractUnmatchedPath.flatMap { path =>
        Logging.requestAuthorizing(cId, path.toString(), method.value)
        checkPermissions(resourceServer, permissions, r => provide((path, method, r.roles)))
      }
    }
  }

  def authorizationFailed(): StandardRoute =
    complete(HttpResponse(UNAUTHORIZED.code, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, UNAUTHORIZED.getMessage)))

}

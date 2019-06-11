package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, provide}
import akka.http.scaladsl.server.StandardRoute.toDirective
import akka.http.scaladsl.server.util.Tuple.yes
import akka.http.scaladsl.server.{Directive, Directive1, StandardRoute}
import com.fullfacing.keycloak4s.auth.akka.http.PayloadImplicits._
import com.fullfacing.keycloak4s.auth.akka.http.models.{AuthPayload, AuthRoles}
import com.fullfacing.keycloak4s.core.Exceptions.UNAUTHORIZED
import com.fullfacing.keycloak4s.core.serialization.JsonFormats._
import org.json4s.jackson.Serialization.write

import scala.annotation.tailrec
import scala.util.matching.Regex

object Utilities {

  private implicit def routeToDirective[A](route: StandardRoute): Directive[A] = {
    toDirective[A](route)(yes[A])
  }

  /**
   * Looks for the requested resource in the user's permissions from the validated access token.
   * The request is rejected if not found.
   *
   * @param resource     The resource the user is attempting to access.
   * @param permissions  The resources and methods allowed for the user.
   * @param success      A directive to create if the user has access to the resource.
   * @return             The resulting directive from the auth result and the function provided.
   */
  def checkPermissions[A](resource: String, permissions: AuthPayload, success: AuthRoles => Directive[A]): Directive[A] = {
    permissions.accessToken.extractResources.find { case (k, _) => k.equalsIgnoreCase(resource) } match {
      case Some((_, v)) => success(v)
      case None         => authorisationFailed()
    }
  }

  def authorisationFailed(): StandardRoute =
    complete(HttpResponse(UNAUTHORIZED.code, entity = HttpEntity(ContentTypes.`application/json`, write(UNAUTHORIZED))))

  def authoriseResourceServerAccess(permissions: AuthPayload, resourceServer: String): Directive1[List[String]] = {
    checkPermissions(resourceServer, permissions, r => provide(r.roles))
  }

  private val validUuid: Regex = """[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r

  @tailrec
  def extractResourcesFromPath(path: Path, acc: List[String] = List.empty[String]): List[String] = {
    lazy val isResource = path.startsWithSegment && validUuid.unapplySeq(path.head.toString).isEmpty

    if (path.isEmpty) acc
    else if (isResource) extractResourcesFromPath(path.tail, acc :+ path.head.toString)
    else extractResourcesFromPath(path.tail, acc)
  }
}

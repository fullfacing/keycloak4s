package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide
import com.fullfacing.keycloak4s.auth.akka.http.directives.AuthorisationDirectives.checkPermissions
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload

import scala.annotation.tailrec
import scala.util.matching.Regex

object Utilities {

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

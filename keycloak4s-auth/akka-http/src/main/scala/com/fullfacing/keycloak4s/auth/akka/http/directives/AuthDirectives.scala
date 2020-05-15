package com.fullfacing.keycloak4s.auth.akka.http.directives

import java.util.UUID

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, extractMatchedPath, extractMethod, extractUnmatchedPath, provide}
import akka.http.scaladsl.server.StandardRoute.toDirective
import akka.http.scaladsl.server.util.Tuple.yes
import akka.http.scaladsl.server.{Directive, Directive1, StandardRoute}
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.auth.core.PayloadImplicits._
import com.fullfacing.keycloak4s.auth.core.models.{AuthPayload, AuthRoles}
import com.fullfacing.keycloak4s.core.Exceptions.UNAUTHORIZED
import com.fullfacing.keycloak4s.core.models.enums.{PathMatchingMode, PathMatchingModes}

trait AuthDirectives {

  /**
   * Looks for the requested resource in the user's permissions from the validated access token.
   * The request is rejected if not found.
   *
   * @param segment      The resource/action the user is attempting to access.
   * @param permissions  The resources and methods allowed for the user.
   * @param success      A directive to create if the user has access to the resource.
   * @return             The resulting directive from the auth result and the function provided.
   */
  def checkPermissions[A](segment: String, permissions: AuthPayload, success: AuthRoles => Directive[A])(implicit cId: UUID): Directive[A] =
    permissions.accessToken.extractResourceAccess.find { case (k, _) => k.equalsIgnoreCase(segment) } match {
      case Some((_, v)) => success(v)
      case None         => Logging.authorizationDenied(cId, segment); authorizationFailed()
    }

  /**
   * Checks the parsed access token to determine whether or not the user has access to the
   * secured resource server / service.
   *
   * @param parsedToken     The user's access token containing their permissions.
   * @param resourceServer  The name of the secured API, as represented by the Keycloak Client.
   */
  def authorizeResourceServerAccess(parsedToken: AuthPayload, resourceServer: String, mode: PathMatchingMode)(implicit cId: UUID): Directive1[(Path, HttpMethod, List[String])] =
    extractMethod.flatMap { method =>
      extractMatchedPath.flatMap { matched =>
        extractUnmatchedPath.flatMap { unmatched =>
          val path = if (mode == PathMatchingModes.Full) matched ++ unmatched else unmatched
          Logging.requestAuthorizing(cId, path.toString(), method.value)
          checkPermissions(resourceServer, parsedToken, r => provide((path, method, r.roles)))
        }
      }
    }

  def authorizationFailed(): StandardRoute =
    complete(HttpResponse(UNAUTHORIZED.code, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, UNAUTHORIZED.getMessage)))

  private implicit def routeToDirective[A](route: StandardRoute): Directive[A] =
    toDirective[A](route)(yes[A])
}

package com.fullfacing.keycloak4s.adapters.akka.http.directives

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.MethodDirectives
import akka.http.scaladsl.server.util.Tuple._
import com.fullfacing.keycloak4s.adapters.akka.http.directives.AuthorisationDirectives._
import com.fullfacing.keycloak4s.adapters.akka.http.directives.magents.{AuthoriseResourceMagnet, WithAuthMagnet}
import com.fullfacing.keycloak4s.adapters.akka.http.models.{Permissions, ResourceMethods}

trait AuthorisationDirectives extends MethodDirectives {
  //TODO Find way to implicitly call Permissions without breaking the subsequent closure.
  /**
   * HTTP methods with checks to ensure the user has the permission to perform the attempted
   * operation on the already authorised resource
   */
  def get(p: ResourceMethods): Directive0    = super.get.tflatMap(_ => authoriseMethod(p))
  def put(p: ResourceMethods): Directive0    = super.put.tflatMap(_ => authoriseMethod(p))
  def head(p: ResourceMethods): Directive0   = super.head.tflatMap(_ => authoriseMethod(p))
  def post(p: ResourceMethods): Directive0   = super.post.tflatMap(_ => authoriseMethod(p))
  def patch(p: ResourceMethods): Directive0  = super.patch.tflatMap(_ => authoriseMethod(p))
  def delete(p: ResourceMethods): Directive0 = super.delete.tflatMap(_ => authoriseMethod(p))

  /** Authorises both the resource and operation */
  def withAuth(magnet: WithAuthMagnet): magnet.Result = magnet()

  /**
   * Uses the permissions from the user's validated token to determine the user's access to this resource.
   *
   * @return The allowed operations available to the user on this resource
   */
  def authoriseResource(magnet: AuthoriseResourceMagnet): magnet.Result = magnet()

  /**
   * Creates a path directive after authorising the user's access to the resource using permissions
   * from the validated access token.
   */
  def pathA[L](resource: String, permissions: Permissions)(pm: PathMatcher[L]): Directive[Tuple1[(ResourceMethods, L)]] = {
    checkPermissions(resource, permissions, v => path(pm).tmap(a => Tuple1((v, a))))
  }
}

object AuthorisationDirectives {

  private implicit def routeToDirective[A](route: StandardRoute): Directive[A] = {
    toDirective[A](route)(yes[A])
  }

  //TODO Determine if OPTIONS method will require authentication.
  def scopeMap(method: HttpMethod): List[String] = method match {
    case HttpMethods.GET    | HttpMethods.HEAD                     => List("delete", "create", "view")
    case HttpMethods.POST   | HttpMethods.PUT  | HttpMethods.PATCH => List("delete", "create")
    case HttpMethods.DELETE                                        => List("delete")
    case _                                                         => List.empty[String]
  }

  /**
   * Looks for the requested resource in the user's permissions from the validated access token.
   * The request is rejected if not found.
   *
   * @param resource     The resource the user is attempting to access.
   * @param permissions  The resources and methods allowed for the user.
   * @param success      A directive to create if the user has access to the resource.
   * @tparam A
   * @return             The resulting directive from the auth result and the function provided.
   */
  def checkPermissions[A](resource: String, permissions: Permissions, success: ResourceMethods => Directive[A]): Directive[A] = {
    permissions.access.find { case (k, _) => k.equalsIgnoreCase(resource) } match {
      case Some((_, v)) => success(v)
      case None         => reject(AuthorizationFailedRejection)
    }
  }

  def authoriseMethod(accessLevel: ResourceMethods): Directive0 = {
    extractMethod.flatMap { method =>
      authorize {
        accessLevel.roles.intersect(scopeMap(method)).nonEmpty
      }
    }
  }
}
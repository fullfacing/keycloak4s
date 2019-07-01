package com.fullfacing.keycloak4s.auth.akka.http.models.path

/**
 * @param path  The for which this rule applies.
 * @param roles The The required roles for the request with this path to be accepted.
 */
final case class PathRoles(path: List[String],
                           roles: List[PathMethodRoles])

object PathRoles {

  final case class Create(path: String,
                          roles: List[PathMethodRoles.Create])

  def apply(path: String, roles: List[PathMethodRoles]): PathRoles =
    PathRoles(path.split("/").toList, roles)
}
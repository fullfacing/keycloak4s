package com.fullfacing.keycloak4s.auth.akka.http.models.path

/**
 * @param path        The path for which this rule applies.
 * @param methodRoles The required roles for the request, for each specified HTTP method.
 */
final case class PathRule(path: List[String],
                          methodRoles: List[PathMethodRoles])

object PathRule {

  final case class Create(path: String,
                          methodRoles: List[PathMethodRoles.Create])

  def apply(path: String, methodRoles: List[PathMethodRoles]): PathRule =
    PathRule(path.split("/").filter(_.nonEmpty).toList, methodRoles)
}
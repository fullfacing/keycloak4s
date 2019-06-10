package com.fullfacing.keycloak4s.auth.akka.http.models

/**
 * @param path  The for which this rule applies.
 * @param roles The The required roles for the request with this path to be accepted.
 */
case class PathRoles(path: List[String],
                     roles: List[PathMethodRoles])

object PathRoles {
  case class Create(path: String,
                    roles: List[PathMethodRoles])

  /**
   * Alternative constructor that reads the path as a string, and converts it to the needed format.
   * {
   *    path = "segment1/segment2/segment3",
   *    roles = [...]
   * }
   */
  def apply(pathRoles: Create): PathRoles = new PathRoles(pathRoles.path.split("/").toList, pathRoles.roles)

  def apply(config: String): PathRoles = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read
    apply(read[Create](config))
  }
}
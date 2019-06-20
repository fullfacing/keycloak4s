package com.fullfacing.keycloak4s.auth.akka.http.models.path

/**
 * @param path  The for which this rule applies.
 * @param roles The The required roles for the request with this path to be accepted.
 */
case class PathRoles(path: List[String],
                     roles: List[PathMethodRoles])

object PathRoles {
  import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
  import org.json4s.jackson.Serialization.read

  case class Create(path: String,
                    roles: List[PathMethodRoles])


  def apply(pathRoles: Create): PathRoles = new PathRoles(pathRoles.path.split("/").toList, pathRoles.roles)

  /**
   * Alternative constructor that reads the path as a string, and converts it to the needed format.
   * {
   *    path = "segment1/segment2/segment3",
   *    roles = [...]
   * }
   */
  def apply(config: String): PathRoles = {
    apply(read[Create](config))
  }
}
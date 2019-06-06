package com.fullfacing.keycloak4s.auth.akka.http.models

case class PathRoles(path: List[String],
                     roles: List[PathMethodRoles])

object PathRoles {
  case class Create(path: String,
                    roles: List[PathMethodRoles])

  def apply(pathRoles: Create): PathRoles = new PathRoles(pathRoles.path.split("/").toList, pathRoles.roles)

  def apply(config: String): PathRoles = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read
    apply(read[Create](config))
  }
}
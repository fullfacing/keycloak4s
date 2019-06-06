package com.fullfacing.keycloak4s.auth.akka.http.models

case class PathRoles(path: List[String],
                     roles: List[PathMethodRoles])

object PathRoles {
  case class Create(path: String,
                    roles: List[PathMethodRoles])

  def apply(path: String,
            roles: List[PathMethodRoles]): PathRoles = new PathRoles(path.split("/").toList, roles)

  def apply(config: String): PathRoles = {
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.read

    val pathRole = read[Create](config)
    apply(pathRole.path, pathRole.roles)
  }
}
package com.fullfacing.keycloak4s.adapters.akka.http.directives

import akka.http.scaladsl.server.{Directive0, Directive1}
import com.fullfacing.keycloak4s.adapters.akka.http.directives.magnets.{SecurityMagnet, ValidateRequestMagnet}
import com.fullfacing.keycloak4s.adapters.akka.http.models.Permissions

trait SecurityDirectives extends ValidationDirective with AuthorisationDirectives {

  /**
   * Top level authorisation on the service.
   *
   * The user's access token is validated and the user permissions are extracted.
   * The permissions are checked to determine access to the service and lower level
   * user permissions specific to the service are returned.
   */
  def secure(magnet: ValidateRequestMagnet): Directive1[Permissions] = magnet()

  def secure(magnet: SecurityMagnet): Directive0 = magnet()
}

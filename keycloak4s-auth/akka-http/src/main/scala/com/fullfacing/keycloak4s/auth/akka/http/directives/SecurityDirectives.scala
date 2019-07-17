package com.fullfacing.keycloak4s.auth.akka.http.directives

import akka.http.scaladsl.server.Directive1
import com.fullfacing.keycloak4s.auth.akka.http.directives.magnets.SecurityMagnet
import com.fullfacing.keycloak4s.auth.akka.http.models.AuthPayload

trait SecurityDirectives extends ValidationDirective {

  /**
   * Authorization directive that secures all inner routes.
   *
   * Using the passed Authorization config object and TokenValidator, the access token and optional ID token
   * are validated and the user permissions extracted. The request is then compared to the configured security
   * policy(Authorization object) to determine the required permissions. The request is then authorized accordingly.
   *
   * Logging: A correlation ID can be passed to the directive, otherwise a new one is generated.
   *
   * @param magnet Either just the config object or a tuple (config, correlationId)
   */
  def secure(magnet: SecurityMagnet): Directive1[AuthPayload] = magnet()
}

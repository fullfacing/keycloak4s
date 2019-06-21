package com.fullfacing.keycloak4s.auth.akka.http.directives

import akka.http.scaladsl.server.Directive0
import com.fullfacing.keycloak4s.auth.akka.http.directives.magnets.SecurityMagnet

trait SecurityDirectives extends ValidationDirective {

  def secure(magnet: SecurityMagnet): Directive0 = magnet()
}

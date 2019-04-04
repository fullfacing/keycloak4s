package com.fullfacing.keycloak4s.adapters.akka.http.apollo

import java.time.Instant
import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{extractClientIP, extractCredentials, optionalHeaderValueByName, provide}
import com.fullfacing.apollo.http.directives.TaskDirectives

trait RequestContextDirectives extends TaskDirectives {

  def context: Directive1[RequestContext] = {
    (for {
      adr <- extractClientIP
      crd <- extractCredentials.map(_.map(_.token()))
      agt <- optionalHeaderValueByName("User-Agent")
      cid <- optionalHeaderValueByName("X-Correlation-Id")
    } yield (adr.toIP, crd, agt, cid)).tflatMap { case (adr, crd, agt, cid) =>
      provide(
        RequestContext(
          ip            = adr,
          agent         = agt,
          token         = crd,
          timestamp     = Instant.now(),
          correlationId = cid.map(UUID.fromString).getOrElse(UUID.randomUUID()))
      )
    }
  }
}
package com.fullfacing.keycloak4s.auth.akka.http.handles

import org.slf4j.{Logger, LoggerFactory}

object Logging {
  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.auth")
}

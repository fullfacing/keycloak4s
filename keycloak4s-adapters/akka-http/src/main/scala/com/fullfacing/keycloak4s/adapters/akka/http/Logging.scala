package com.fullfacing.keycloak4s.adapters.akka.http

import org.slf4j.{Logger, LoggerFactory}

object Logging {
  implicit val logger: Logger = LoggerFactory.getLogger("Application")
}

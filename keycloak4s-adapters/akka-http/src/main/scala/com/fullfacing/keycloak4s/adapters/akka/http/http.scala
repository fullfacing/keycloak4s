package com.fullfacing.keycloak4s.adapters.akka

import org.slf4j.{Logger, LoggerFactory}

package object http {
  implicit val logger: Logger = LoggerFactory.getLogger("apollo.http")
}

package com.fullfacing.keycloak4s.core

import org.slf4j.{Logger, LoggerFactory}

package object logging {
  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s-core")
}

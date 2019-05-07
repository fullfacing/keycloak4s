package com.fullfacing.keycloak4s.core.logging

import org.slf4j.Logger

object Logging {
  implicit class Levels(private val logger: Logger) {
    def writeTrace(msg: String): Unit = {
      if (logger.isTraceEnabled()) logger.trace(msg)
    }

    def writeDebug(msg: String): Unit = {
      if (logger.isDebugEnabled()) logger.debug(msg)
    }

    def writeInfo(msg: String): Unit = {
      if (logger.isInfoEnabled()) logger.info(msg)
    }
  }
}

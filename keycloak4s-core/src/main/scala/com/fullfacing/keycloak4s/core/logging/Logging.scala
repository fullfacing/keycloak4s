package com.fullfacing.keycloak4s.core.logging

import org.slf4j.Logger

object Logging {
  implicit class Levels(private val logger: Logger) {
    def logTrace(msg: String): Unit = {
      if (logger.isTraceEnabled()) logger.trace(msg)
    }

    def logDebug(msg: String): Unit = {
      if (logger.isDebugEnabled()) logger.debug(msg)
    }

    def logDebugIff(msg: String): Unit = {
      if (logger.isDebugEnabled() && !logger.isTraceEnabled()) logger.debug(msg)
    }

    def logInfo(msg: String): Unit = {
      if (logger.isInfoEnabled()) logger.info(msg)
    }

    def logInfoIff(msg: String): Unit = {
      if (logger.isInfoEnabled() & !logger.isDebugEnabled()) logger.info(msg)
    }
  }
}

package com.fullfacing.keycloak4s.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.spi.FilterReply

class StdErrFilter extends AbstractMatcherFilter[ILoggingEvent] {
  private val levels: List[Level] = List(Level.WARN, Level.ERROR)
  override def decide(event: ILoggingEvent): FilterReply = {
    if (!isStarted || levels.contains(event.getLevel)) FilterReply.ACCEPT else FilterReply.DENY
  }
}

package com.fullfacing.transport.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.spi.FilterReply

class StdOutFilter extends AbstractMatcherFilter[ILoggingEvent] {
  private val levels: List[Level] = List(Level.TRACE, Level.DEBUG, Level.INFO)
  override def decide(event: ILoggingEvent): FilterReply = {
    if (!isStarted || levels.contains(event.getLevel)) FilterReply.ACCEPT else FilterReply.DENY
  }
}

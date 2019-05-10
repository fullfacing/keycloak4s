package com.fullfacing.keycloak4s.auth.akka.http.handles

import java.util.UUID

import com.fullfacing.keycloak4s.core.logging.Logging._
import com.fullfacing.keycloak4s.core.logging._
import org.slf4j.{Logger, LoggerFactory}

object Logging {

  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.auth")

  /* VALIDATION LOGGING **/
  def jwksRequest(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Requesting public keys from Keycloak for Realm $gr$realm$rs.")

  def jwksRetrieved(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Public keys retrieved for Realm $gr$realm$rs.")

  def jwksRequestFailed(cId: => UUID, ex: Throwable): Unit =
    logger.error(cIdLog(cId), ex)


}

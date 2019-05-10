package com.fullfacing.keycloak4s.auth.akka.http.handles

import java.util.UUID

import com.fullfacing.keycloak4s.core.logging.Logging._
import com.fullfacing.keycloak4s.core.logging._
import com.fullfacing.keycloak4s.core.models.KeycloakException
import org.slf4j.{Logger, LoggerFactory}

object Logging {

  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.auth")

  /* VALIDATION LOGGING **/
  def jwksRequest(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Requesting public keys from Keycloak for Realm $gr$realm$rs.")

  def jwksRetrieved(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Public keys retrieved for Realm $gr$realm$rs.")

  def jwksRequestFailed(cId: => UUID, ex: Throwable): Unit =
    logger.error(s"${cIdLog(cId)}$rs", ex)

  def tokenValidating(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Validating bearer token...")

  def tokenValidated(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Bearer token successfully validated.")

  def tokenValidationFailed(cId: => UUID, ex: Throwable): Unit =
    logger.error(s"${cIdLog(cId)}${rs}Token validation failed.", ex)

  /* Logging Helper **/
  def logException(exception: KeycloakException)(log: => Unit): KeycloakException = {
    log; exception
  }

  def logValidationException(exception: KeycloakException)(implicit cId: UUID): KeycloakException = {
    Logging.tokenValidationFailed(cId, exception)
    exception
  }
}

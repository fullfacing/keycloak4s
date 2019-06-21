package com.fullfacing.keycloak4s.auth.akka.http.handles

import java.util.UUID

import com.fullfacing.keycloak4s.core.logging.Logging._
import com.fullfacing.keycloak4s.core.logging._
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.fullfacing.keycloak4s.core.models.enums.TokenType
import org.slf4j.{Logger, LoggerFactory}

object Logging {

  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.auth")

  /* VALIDATION LOGGING **/
  def jwksRequest(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Requesting public keys from Keycloak for Realm $gr$realm.$rs")

  def jwksCache(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Cached public keys for Realm $gr$realm$cy found.$rs")

  def jwksRetrieved(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Public keys retrieved for Realm $gr$realm.$rs")

  def jwksRequestFailed(cId: => UUID, ex: Throwable): Unit =
    logger.error(s"${cIdErr(cId)}", ex)

  def tokenValidating(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Validating bearer token...$rs")

  def tokenValidated(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Bearer token(s) successfully validated.$rs")

  def tokenValidationFailed(cId: => UUID, ex: Throwable, tokenType: TokenType): Unit =
    logger.error(s"${cIdErr(cId)}${tokenType.value} validation failed.", ex)

  def tokenValidationFailed(cId: => UUID, exMessage: String, tokenType: TokenType): Unit =
    logger.error(s"${cIdErr(cId)}${tokenType.value} validation failed - $exMessage")

  def requestAuthorising(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Authorising request...$rs")

  def requestAuthorised(cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Authorisation successful.$rs")

  def authorisationFailed(cId: => UUID): Unit =
    logger.logDebug(s"$re${cIdErr(cId)}Authorisation failed. Request rejected.$rs")

  def configSetupError(): Unit =
    logger.error("Could not parse json string into policy configuration object")

  def authResourceNotFound(ar: => String): Unit =
    logger.error(s"${re}Auth Initialisation failed: No $ar auth resource found.$rs")

  /* Logging Helper **/
  def logException(exception: KeycloakException)(log: => Unit): KeycloakException = {
    log; exception
  }

  def logValidationEx(exception: KeycloakException, tokenType: TokenType)(implicit cId: UUID): KeycloakException = {
    if (exception.details.isDefined) {
      Logging.tokenValidationFailed(cId, exception, tokenType)
    } else {
      Logging.tokenValidationFailed(cId, exception.message.getOrElse("An unexpected error occurred."), tokenType)
    }

    exception
  }
}

package com.fullfacing.keycloak4s.auth.akka.http

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

  def resourceAuthorizing(resource: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Checking resource $gr$resource ${cy}authorization...$rs")

  def resourceAuthorized(resource: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Resource $gr$resource ${cy}authorized.$rs")

  def resourceAuthorizationFailed(resource: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdErr(cId)}Resource $resource is not authorized.")

  def methodAuthorizing(method: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Checking method $gr$method ${cy}authorization...$rs")

  def methodAuthorized(method: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Method $gr$method ${cy}authorized.$rs")

  def methodAuthorizationFailed(method: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdErr(cId)}Method $method is not authorized.")

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

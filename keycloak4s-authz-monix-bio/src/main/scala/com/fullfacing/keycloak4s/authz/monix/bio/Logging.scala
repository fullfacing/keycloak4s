package com.fullfacing.keycloak4s.authz.monix.bio

import com.fullfacing.keycloak4s.core.logging.Logging.Levels
import com.fullfacing.keycloak4s.core.logging._
import com.fullfacing.keycloak4s.core.models.RequestInfo
import org.slf4j.{Logger, LoggerFactory}

import java.util.UUID

object Logging {
  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.authz")

  /* ACCESS TOKEN LOGGING **/

  def tokenRequest(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Requesting Access Token from Keycloak for Realm $gr$realm$cy.$rs")

  def tokenReceived(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Access Token retrieved for Realm $gr$realm$cy.$rs")

  def tokenRequestFailed(realm: String, cId: UUID, ex: Throwable): Unit =
    logger.error(s"${cIdErr(cId)}Access Token could not be retrieved for Realm $realm.", ex)

  /* REFRESH TOKEN LOGGING **/

  def tokenRefresh(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Refreshing Access Token for Realm $gr$realm$cy.$rs")

  /* AUTHZ API REST LOGGING **/

  def requestSent(requestInfo: => RequestInfo, cId: => UUID): Unit = {
    lazy val bodyTrace = requestInfo.body.fold("")(b => s"Body: \n$gr$b")

    logger.logDebugIff(s"${cIdLog(cId)}$gr${requestInfo.protocol}$cy request sent to Keycloak Authz API.$rs")
    logger.logTrace(s"${cIdLog(cId)}$gr${requestInfo.protocol} ${requestInfo.path}$cy request sent to Keycloak Authz API. $bodyTrace$rs")
  }

  def retryUnauthorized(requestInfo: => RequestInfo, cId: => UUID): Unit = {
    logger.warn(s"${cIdLog(cId)}$gr${requestInfo.protocol}$cy request failed with an authorization error. Retrying...$rs")
  }

  def requestSuccessful(response: => String, cId: => UUID): Unit = {
    lazy val resp = if (response == "") "NoContent" else response
    logger.logDebugIff(s"${cIdLog(cId)}Request was successful.$rs")
    logger.logTrace(s"${cIdLog(cId)}Request was successful. Response received: $resp$rs")
  }

  def requestFailed(cId: UUID, ex: Throwable): Unit =
    logger.error(s"${cIdErr(cId)}Request to Keycloak Authz API failed.", ex)
}

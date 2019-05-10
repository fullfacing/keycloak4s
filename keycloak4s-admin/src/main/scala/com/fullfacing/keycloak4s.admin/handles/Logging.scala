package com.fullfacing.keycloak4s.admin.handles

import java.util.UUID

import com.fullfacing.keycloak4s.core.logging.Logging._
import com.fullfacing.keycloak4s.core.logging._
import com.fullfacing.keycloak4s.core.models.RequestInfo
import org.slf4j.{Logger, LoggerFactory}

object Logging {

  implicit val logger: Logger = LoggerFactory.getLogger("keycloak4s.admin")

  /* ACCESS TOKEN LOGGING **/

  def tokenRequest(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Requesting Access Token from Keycloak for Realm $gr$realm$rs.")

  def tokenReceived(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Access Token retrieved for Realm $gr$realm$rs.")

  def tokenRequestFailed(realm: String, cId: UUID, ex: Throwable): Unit =
    logger.error(s"Correlation ID: $cId - Access Token could not be retrieved for Realm $realm.", ex)

  /* REFRESH TOKEN LOGGING **/

  def tokenRefresh(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Refreshing Access Token for Realm $gr$realm$rs.")

  def tokenRefreshed(realm: => String, cId: => UUID): Unit =
    logger.logTrace(s"${cIdLog(cId)}Access Token refreshed for Realm $gr$realm$rs.")

  def tokenRefreshFailed(realm: String, cId: UUID, ex: Throwable): Unit =
    logger.error(s"Correlation ID: $cId - Access Token could not be refreshed for Realm $realm.", ex)

  /* ADMIN API REST LOGGING **/

  def requestSent(requestInfo: => RequestInfo, cId: => UUID): Unit = {
    lazy val bodyTrace = requestInfo.body.fold("")(b => s"Body: \n$gr$b")

    logger.logDebugIff(s"${cIdLog(cId)}$gr${requestInfo.protocol}$cy request sent to Keycloak Admin API.$rs")
    logger.logTrace(s"${cIdLog(cId)}$gr${requestInfo.protocol} ${requestInfo.path}$cy request sent to Keycloak Admin API. $bodyTrace$rs")
  }

  def requestSuccessful[A](response: A, cId: => UUID): Unit = {
    //lazy val bodyTrace: String = if (response.toString == "") s"${gr}NoContent" else s"$gr$response" TODO Fix serialization error.

    logger.logDebugIff(s"${cIdLog(cId)}Request was successful.$rs")
    logger.logTrace(s"${cIdLog(cId)}Request was successful.$rs")
  }

  def requestFailed(cId: UUID, ex: Throwable): Unit =
    logger.error(s"Correlation ID: $cId - Request to Keycloak Admin API failed.", ex)

  /* Logging Helper **/
  def handleLogging[A, B <: Throwable](resp: Either[B, A])(success: A => Unit, failure: B => Unit): Either[B, A] = resp match {
    case Left(e)  => failure(e); resp
    case Right(r) => success(r); resp
  }
}
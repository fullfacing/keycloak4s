package com.fullfacing.keycloak4s.auth.akka.http.directives.magnets

import java.util.UUID

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractMethod, extractUnmatchedPath, pass, _}
import cats.effect.IO
import com.fullfacing.keycloak4s.auth.akka.http.Logging
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.Authorisation
import com.fullfacing.keycloak4s.auth.akka.http.authorisation.Authorisation._
import com.fullfacing.keycloak4s.auth.akka.http.directives.Directives._
import com.fullfacing.keycloak4s.auth.akka.http.validation.TokenValidator

import scala.util.Success

trait SecurityMagnet {
  def apply(): Directive0
}

object SecurityMagnet {

  implicit def run(parameters: (Authorisation, UUID))(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    val (securityConfig, cId) = parameters
    authorise(securityConfig)(tokenValidator, cId)
  }

  implicit def run(securityConfig: Authorisation)(implicit tokenValidator: TokenValidator): SecurityMagnet = { () =>
    onComplete(IO(UUID.randomUUID()).unsafeToFuture()).flatMap {
      case Success(correlationId) => authorise(securityConfig)(tokenValidator, correlationId)
      case _                      => reject
    }
  }

  private def authorise(securityConfig: Authorisation)(implicit tokenValidator: TokenValidator, cId: UUID): Directive0 = {
    validateToken().flatMap { authPayload =>
      if (securityConfig.policyDisabled()) {
        pass
      } else {
        authoriseResourceServerAccess(authPayload, securityConfig.service).flatMap { userRoles =>
          extractUnmatchedPath.flatMap { path =>
            extractMethod.flatMap { method =>
              Logging.requestAuthorising(cId)
              val isAuthorised = securityConfig.authoriseRequest(path, method, userRoles)

              if (isAuthorised) {
                Logging.requestAuthorised(cId)
                pass
              } else {
                authorisationFailed()
              }
            }
          }
        }
      }
    }
  }
}
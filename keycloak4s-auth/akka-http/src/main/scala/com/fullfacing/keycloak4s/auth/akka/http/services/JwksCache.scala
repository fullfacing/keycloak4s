package com.fullfacing.keycloak4s.auth.akka.http.services

import java.net.URL
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logException
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.jwk.JWKSet

abstract class JwksCache(host: String, port: String, realm: String) {

  /* The URL to retrieve the ConnectID JWKS. **/
  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The cached JWK set. **/
  private val ref: AtomicReference[Either[KeycloakException, JWKSet]] = new AtomicReference()

  /* Retrieves a JWK set, caches it and returns it. **/
  private def cacheKeys()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = IO {
    Logging.jwksRequest(realm, cId)
    val jwks = JWKSet.load(url).asRight[KeycloakException]
    Logging.jwksRetrieved(realm, cId)

    ref.set(jwks)
    jwks
  }

  /* Retrieves the JWK set asynchronously and (re)caches it. Caches the exception in case of failure. **/
  protected def updateCache()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = cacheKeys().handleError { _ =>
    ref.set(Exceptions.JWKS_SERVER_ERROR.asLeft[JWKSet])

    logException(Exceptions.JWKS_SERVER_ERROR) {
      Logging.jwksRequestFailed(cId, Exceptions.JWKS_SERVER_ERROR)
    }.asLeft
  }

  /* Retrieves the cached value. Recaches if empty. **/
  protected def retrieveCachedValue()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = IO {
    Option(ref.get).fold(updateCache()) { jwks =>
      IO {
        Logging.jwksCache(realm, cId)
        jwks
      }
    }
  }.flatten
}

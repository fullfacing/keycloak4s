package com.fullfacing.keycloak4s.auth.akka.http.services

import java.net.URL
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.akka.http.handles.Logging.logger
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.fullfacing.keycloak4s.core.utilities.IoImplicits._
import com.nimbusds.jose.jwk.JWKSet

abstract class JwksCache (host: String, port: String, realm: String) {

  /* The URL to retrieve the ConnectID JWKS. **/
  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The cached JWK set. **/
  private val ref: AtomicReference[Either[KeycloakException, JWKSet]] = new AtomicReference()

  /* Retrieves a JWK set, caches it and returns it. **/
  private def cacheKeys(): IO[Either[KeycloakException, JWKSet]] = IO {
    val jwks = JWKSet.load(url).asRight[KeycloakException]
    ref.set(jwks)
    jwks
  }

  /* Retrieves the JWK set asynchronously and (re)caches it. Caches the exception in case of failure. **/
  protected def updateCache(): IO[Either[KeycloakException, JWKSet]] = cacheKeys().handleErrorWithLogging { _ =>
    ref.set(Exceptions.JWKS_SERVER_ERROR.asLeft[JWKSet])
    Exceptions.JWKS_SERVER_ERROR.asLeft
  }

  /* Retrieves the cached value. Recaches if empty. **/
  protected def retrieveCachedValue(): IO[Either[KeycloakException, JWKSet]] = IO {
    Option(ref.get).fold(updateCache())(IO(_))
  }.flatten
}

package com.fullfacing.keycloak4s.adapters.akka.http.services

import java.net.URL
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.nimbusds.jose.jwk.JWKSet

abstract class JwksCache (host: String, port: String, realm: String) {

  /* The URL to retrieve the ConnectID JWKS. **/
  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The cached JWK set. **/
  private val ref: AtomicReference[Either[Throwable, JWKSet]] = new AtomicReference()

  /* Retrieves a JWK set, caches it and returns it. **/
  private def cacheKeys(): IO[Either[Throwable, JWKSet]] = IO {
    val jwks = JWKSet.load(url).asRight[Throwable]
    ref.set(jwks)
    jwks
  }

  /* Retrieves the JWK set asynchronously and (re)caches it. Caches the exception in case of failure. **/
  protected def updateCache(): IO[Either[Throwable, JWKSet]] = cacheKeys().handleError { ex =>
    ref.set(ex.asLeft[JWKSet])
    ex.asLeft
  }

  /* Retrieves the cached value. Recaches if empty. **/
  protected def retrieveCachedValue(): IO[Either[Throwable, JWKSet]] = IO {
    Option(ref.get).fold(updateCache())(IO(_))
  }.flatten
}

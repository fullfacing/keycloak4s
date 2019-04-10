package com.fullfacing.keycloak4s.adapters.akka.http

import java.net.URL
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.nimbusds.jose.jwk.JWKSet

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

abstract class JwksCache (host: String, port: String, realm: String)(implicit ec: ExecutionContext) {

  /* The URL to retrieve the ConnectID JWKS. **/
  private val url = new URL(s"http://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The cached JWK set. **/
  private val ref: AtomicReference[Either[Throwable, JWKSet]] = new AtomicReference()

  /* Retrieves a JWK set, caches it and returns it. **/
  private def cacheKeys(): Either[Throwable, JWKSet] = {
    val jwks = JWKSet.load(url).asRight[Throwable]
    ref.set(jwks)
    jwks
  }

  /* Caches and returns an exception. **/
  private def cacheException(ex: Throwable): Either[Throwable, JWKSet] = {
    ref.set(ex.asLeft[JWKSet])
    ex.asLeft
  }

  /* Retrieves the JWK set asynchronously and (re)caches it. **/
  def updateCache(): IO[Either[Throwable, JWKSet]] = IO.async[JWKSet] { cb =>
    ec.execute { () =>
      try cb(cacheKeys()) catch { case NonFatal(ex) => cb(cacheException(ex)) }
    }
  }.map(_.asRight[Throwable]).handleError(_.asLeft)

  /* Retrieves the cached value. Recaches if empty. **/
  def retrieveCachedValue(): IO[Either[Throwable, JWKSet]] = IO {
    Option(ref.get).fold(updateCache())(IO(_))
  }.flatten
}

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

  /* Retrieves the JWK set asynchronously and (re)caches it. **/
  def recache(): IO[Either[Throwable, JWKSet]] = IO.async[JWKSet] { cb =>
    ec.execute(() =>
      try {
        val jkws = JWKSet.load(url).asRight[Throwable]
        ref.set(jkws)
        cb(jkws)
      } catch { case NonFatal(ex) =>
        ref.set(ex.asLeft)
        cb(ex.asLeft)
      })
  }.map(_.asRight[Throwable]).handleError(_.asLeft)

  /* Retrieves the cached value. Recaches if empty. **/
  def getCachedValue()(implicit e: ExecutionContext): IO[Either[Throwable, JWKSet]] = IO {
    Option(ref.get).fold(recache())(IO(_))
  }.flatten
}

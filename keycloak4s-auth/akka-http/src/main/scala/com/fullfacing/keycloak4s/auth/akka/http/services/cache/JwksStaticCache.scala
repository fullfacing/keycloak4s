package com.fullfacing.keycloak4s.auth.akka.http.services.cache

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.jwk.JWKSet

trait JwksStaticCache extends JwksCache {

  protected def jwks: JWKSet

  protected val ref = new AtomicReference[Either[KeycloakException, JWKSet]](jwks.asRight[KeycloakException])

  protected def getCachedValue()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = IO(ref.get())

  protected def attemptRecache()(implicit ciD: UUID): IO[Unit] = IO.unit
}

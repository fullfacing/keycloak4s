package com.fullfacing.keycloak4s.auth.akka.http.validation.cache

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import com.fullfacing.keycloak4s.core.models.{KeycloakConfig, KeycloakException}
import com.nimbusds.jose.jwk.JWKSet

trait JwksCache {

  /* The Keycloak server configuration required for dynamic caching. **/
  protected def config: KeycloakConfig

  /* The JWK set cache. **/
  protected val ref: AtomicReference[Either[KeycloakException, JWKSet]]

  /* Returns the JWK set from the cache, or an exception if there is no JWK set available. **/
  protected def getCachedValue()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]]

  /* If possible forces the cache to re-retrieve the JWK set. Otherwise does nothing. **/
  protected def attemptRecache()(implicit cId: UUID): IO[Unit]
}

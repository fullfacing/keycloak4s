package com.fullfacing.keycloak4s.auth.core.validation.cache

import java.net.URL
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.auth.core.Logging
import com.fullfacing.keycloak4s.auth.core.Logging.logException
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakException
import com.nimbusds.jose.jwk.JWKSet

trait JwksDynamicCache extends JwksCache {

  /* The URL to retrieve the Keycloak server's JWKS. **/
  private val url = new URL(s"${config.buildBaseUri}/realms/${config.realm}/protocol/openid-connect/certs")

  /* The cached response received from the Keycloak JWK set endpoint. **/
  protected val ref: AtomicReference[Either[KeycloakException, JWKSet]] = new AtomicReference[Either[KeycloakException, JWKSet]]()

  /* Retrieves and cached a JWK set from the Keycloak server. **/
  private def retrieveJwks()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = IO {
    Logging.jwksRequest(config.realm, cId)
    val jwks = JWKSet.load(url).asRight[KeycloakException]
    Logging.jwksRetrieved(config.realm, cId)

    ref.set(jwks)
    jwks
  }.handleError(processError)

  /* Error handling for a JWKS retrieval failure. **/
  private def processError(thr: Throwable)(implicit cId: UUID): Either[KeycloakException, JWKSet] = {
    val ex = Exceptions.JWKS_SERVER_ERROR(thr.getMessage)
    ref.set(ex.asLeft[JWKSet])

    logException(ex) {
      Logging.jwksRequestFailed(cId, ex)
    }.asLeft
  }

  /* Retrieves the cached value. Re-caches once if empty or an Exception. **/
  protected def getCachedValue()(implicit cId: UUID): IO[Either[KeycloakException, JWKSet]] = {
    Option(ref.get) match {
      case Some(jwks @ Right(_))  => IO(jwks)
      case _                      => retrieveJwks()
    }
  }

  /* Forces the cache to retrieve the JWKS from the Keycloak server. **/
  override protected def attemptRecache()(implicit cId: UUID): IO[Unit] = retrieveJwks.map(_ => ())
}

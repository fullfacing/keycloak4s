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
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}

import collection.JavaConverters._

abstract class VerifierCache(scheme: String, host: String, port: Int, realm: String) {

  type VerifierMap = Map[String, RSASSAVerifier]

  /* The URL to retrieve the Keycloak server's JWKS. **/
  private val url = new URL(s"$scheme://$host:$port/auth/realms/$realm/protocol/openid-connect/certs")

  /* The cached RSA Verifier Map. **/
  private val ref: AtomicReference[Either[KeycloakException, VerifierMap]] = new AtomicReference()

  /* Retrieves a JWK set, creates and caches a RSA Verifier Map with its keys. **/
  private def cacheKeys()(implicit cId: UUID): IO[Either[KeycloakException, VerifierMap]] = IO {
    Logging.jwksRequest(realm, cId)

    val jwks = JWKSet.load(url).getKeys.asScala

    val verifiers = jwks.map { case key: RSAKey =>
      (key.getKeyID, new RSASSAVerifier(key))
    }.toMap.asRight[KeycloakException]

    Logging.jwksRetrieved(realm, cId)

    ref.set(verifiers)
    verifiers
  }

  /* Creates or updates the RSA Verifier Map cache with additional error handling. **/
  protected def updateCache()(implicit cId: UUID): IO[Either[KeycloakException, VerifierMap]] = cacheKeys().handleError { thr =>
    val ex = Exceptions.JWKS_SERVER_ERROR(thr.getMessage)

    ref.set(ex.asLeft[VerifierMap])

    logException(ex) {
      Logging.jwksRequestFailed(cId, ex)
    }.asLeft
  }

  /* Retrieves the cached value. Re-caches if empty. **/
  protected def retrieveCachedValue()(implicit cId: UUID): IO[Either[KeycloakException, VerifierMap]] = IO {
    Option(ref.get).fold(updateCache())(IO(_))
  }.flatten
}

package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.protocol.internal.ErrorPayload
import com.fullfacing.keycloak4s.SttpClient
import monix.eval.Task

import scala.collection.immutable.Seq

object AuthenticationManagement {

  def getAuthenticationProviders(realm: String): Task[Either[ErrorPayload, Map[Any, Any]]] = {

  }
}

package com.fullfacing.keycloak4s.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.services.{AttackDetection, AuthenticationManagement, Clients, Users}

object Keycloak {
  def Users[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Users[R, S]
  def Clients[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Clients[R, S]
  def AttackDetection[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AttackDetection[R, S]
  def AuthenticationManagement[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AuthenticationManagement[R, S]
}
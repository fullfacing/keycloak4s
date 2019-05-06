package com.fullfacing.keycloak4s.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.services._

object Keycloak {

  def Keys[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Keys[R, S]
  def Root[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Root[R, S]

  def Components[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Components[R, S]

  def RealmsAdmin[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new RealmsAdmin[R, S]
  def ScopeMappings[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new ScopeMappings[R, S]
  def ProtocolMappers[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new ProtocolMappers[R, S]
  def AttackDetection[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AttackDetection[R, S]
  def IdentityProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new IdentityProviders[R, S]
  def UserStorageProviders[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new UserStorageProviders[R, S]
  def AuthenticationManagement[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AuthenticationManagement[R, S]

  def Users[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) = new Users[R, S](realm)
  def Roles[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) = new Roles[R, S](realm)
  def Groups[R[+_]: Concurrent, S](realm: String)(implicit client: KeycloakClient[R, S]) = new Groups[R, S](realm)
  def RolesById[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new RolesById[R, S]

  def Clients[R[+_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Clients[R, S]
}
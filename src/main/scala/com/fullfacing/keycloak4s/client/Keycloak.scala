package com.fullfacing.keycloak4s.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.services._

object Keycloak {
  def Root[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Root[R, S]
  def Roles[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Roles[R, S]
  def Users[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Users[R, S]
  def Clients[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new Clients[R, S]
  def RolesById[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new RolesById[R, S]
  def RoleMapper[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new RoleMapper[R, S]
  def ScopeMappings[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new ScopeMappings[R, S]
  def AttackDetection[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AttackDetection[R, S]
  def UserStorageProvider[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new UserStorageProvider[R, S]
  def AuthenticationManagement[R[_]: Concurrent, S](implicit client: KeycloakClient[R, S]) = new AuthenticationManagement[R, S]
}
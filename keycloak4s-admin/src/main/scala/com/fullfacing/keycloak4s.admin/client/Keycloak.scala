package com.fullfacing.keycloak4s.admin.client

import cats.effect.Concurrent
import com.fullfacing.keycloak4s.admin.services._

object Keycloak {

  def AttackDetection[R[+_]         : Concurrent](implicit client: KeycloakClient[R]) = new AttackDetection[R]
  def AuthenticationManagement[R[+_]: Concurrent](implicit client: KeycloakClient[R]) = new AuthenticationManagement[R]
  def Clients[R[+_]                 : Concurrent](implicit client: KeycloakClient[R]) = new Clients[R]
  def ClientScopes[R[+_]            : Concurrent](implicit client: KeycloakClient[R]) = new ClientScopes[R]
  def Components[R[+_]              : Concurrent](implicit client: KeycloakClient[R]) = new Components[R]
  def Groups[R[+_]                  : Concurrent](implicit client: KeycloakClient[R]) = new Groups[R]
  def IdentityProviders[R[+_]       : Concurrent](implicit client: KeycloakClient[R]) = new IdentityProviders[R]
  def Keys[R[+_]                    : Concurrent](implicit client: KeycloakClient[R]) = new Keys[R]
  def ProtocolMappers[R[+_]         : Concurrent](implicit client: KeycloakClient[R]) = new ProtocolMappers[R]
  def RealmsAdmin[R[+_]             : Concurrent](implicit client: KeycloakClient[R]) = new RealmsAdmin[R]
  def Roles[R[+_]                   : Concurrent](implicit client: KeycloakClient[R]) = new Roles[R]
  def RolesById[R[+_]               : Concurrent](implicit client: KeycloakClient[R]) = new RolesById[R]
  def Root[R[+_]                    : Concurrent](implicit client: KeycloakClient[R]) = new Root[R]
  def Users[R[+_]                   : Concurrent](implicit client: KeycloakClient[R]) = new Users[R]
  def UserStorageProviders[R[+_]    : Concurrent](implicit client: KeycloakClient[R]) = new UserStorageProviders[R]
}
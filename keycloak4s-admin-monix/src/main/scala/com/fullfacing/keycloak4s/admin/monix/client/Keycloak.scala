package com.fullfacing.keycloak4s.admin.monix.client

import com.fullfacing.keycloak4s.admin.monix.services._

object Keycloak {
  def AttackDetection[T](implicit client: KeycloakClient[T])          = new AttackDetection
  def AuthenticationManagement[T](implicit client: KeycloakClient[T]) = new AuthenticationManagement
  def Clients[T](implicit client: KeycloakClient[T])                  = new Clients
  def ClientScopes[T](implicit client: KeycloakClient[T])             = new ClientScopes
  def Components[T](implicit client: KeycloakClient[T])               = new Components
  def Groups[T](implicit client: KeycloakClient[T])                   = new Groups
  def IdentityProviders[T](implicit client: KeycloakClient[T])        = new IdentityProviders
  def Keys[T](implicit client: KeycloakClient[T])                     = new Keys
  def ProtocolMappers[T](implicit client: KeycloakClient[T])          = new ProtocolMappers
  def RealmsAdmin[T](implicit client: KeycloakClient[T])              = new RealmsAdmin
  def Roles[T](implicit client: KeycloakClient[T])                    = new Roles
  def RolesById[T](implicit client: KeycloakClient[T])                = new RolesById
  def Root[T](implicit client: KeycloakClient[T])                     = new Root
  def Users[T](implicit client: KeycloakClient[T])                    = new Users
  def UserStorageProviders[T](implicit client: KeycloakClient[T])     = new UserStorageProviders
}
package com.fullfacing.keycloak4s.admin.monix.bio.client

import com.fullfacing.keycloak4s.admin.monix.bio.services._

object Keycloak {
  def AttackDetection[S](implicit client: KeycloakClient[S]) = new AttackDetection[S]
  def AuthenticationManagement[S](implicit client: KeycloakClient[S]) = new AuthenticationManagement[S]
  def Clients[S](implicit client: KeycloakClient[S]) = new Clients[S]
  def Components[S](implicit client: KeycloakClient[S]) = new Components[S]
  def Groups[S](implicit client: KeycloakClient[S]) = new Groups[S]
  def IdentityProviders[S](implicit client: KeycloakClient[S]) = new IdentityProviders[S]
  def Keys[S](implicit client: KeycloakClient[S]) = new Keys[S]
  def ProtocolMappers[S](implicit client: KeycloakClient[S]) = new ProtocolMappers[S]
  def RealmsAdmin[S](implicit client: KeycloakClient[S]) = new RealmsAdmin[S]
  def Roles[S](implicit client: KeycloakClient[S]) = new Roles[S]
  def RolesById[S](implicit client: KeycloakClient[S]) = new RolesById[S]
  def Root[S](implicit client: KeycloakClient[S]) = new Root[S]
  def Users[S](implicit client: KeycloakClient[S]) = new Users[S]
  def UserStorageProviders[S](implicit client: KeycloakClient[S]) = new UserStorageProviders[S]
}
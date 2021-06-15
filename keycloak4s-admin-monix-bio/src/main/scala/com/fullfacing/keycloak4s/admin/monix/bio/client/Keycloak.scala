package com.fullfacing.keycloak4s.admin.monix.bio.client

import com.fullfacing.keycloak4s.admin.monix.bio.services._

object Keycloak {
  def AttackDetection(implicit client: KeycloakClient) = new AttackDetection
  def AuthenticationManagement(implicit client: KeycloakClient) = new AuthenticationManagement
  def Clients(implicit client: KeycloakClient) = new Clients
  def Components(implicit client: KeycloakClient) = new Components
  def Groups(implicit client: KeycloakClient) = new Groups
  def IdentityProviders(implicit client: KeycloakClient) = new IdentityProviders
  def Keys(implicit client: KeycloakClient) = new Keys
  def ProtocolMappers(implicit client: KeycloakClient) = new ProtocolMappers
  def RealmsAdmin(implicit client: KeycloakClient) = new RealmsAdmin
  def Roles(implicit client: KeycloakClient) = new Roles
  def RolesById(implicit client: KeycloakClient) = new RolesById
  def Root(implicit client: KeycloakClient) = new Root
  def Users(implicit client: KeycloakClient) = new Users
  def UserStorageProviders(implicit client: KeycloakClient) = new UserStorageProviders
}
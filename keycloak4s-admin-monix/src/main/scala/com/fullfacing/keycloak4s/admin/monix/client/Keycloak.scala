package com.fullfacing.keycloak4s.admin.monix.client

import com.fullfacing.keycloak4s.admin.monix.services._

object Keycloak {
  def Users(implicit client: KeycloakClient) = new Users
  def Roles(implicit client: KeycloakClient) = new Roles
  def Groups(implicit client: KeycloakClient) = new Groups
  def Clients(implicit client: KeycloakClient) = new Clients
  def RealmsAdmin(implicit client: KeycloakClient) = new RealmsAdmin
}
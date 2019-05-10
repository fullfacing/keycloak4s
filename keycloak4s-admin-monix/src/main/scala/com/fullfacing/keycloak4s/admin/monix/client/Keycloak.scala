package com.fullfacing.keycloak4s.admin.monix.client

import com.fullfacing.keycloak4s.admin.monix.services._

object Keycloak {
  def Keys(implicit client: KeycloakClient) = new Keys
  def Groups(implicit client: KeycloakClient) = new Groups
  def Users(implicit client: KeycloakClient) = new Users
}
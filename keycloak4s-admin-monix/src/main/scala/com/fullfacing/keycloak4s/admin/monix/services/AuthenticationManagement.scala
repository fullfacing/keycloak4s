package com.fullfacing.keycloak4s.admin.monix.services

import akka.util.ByteString
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task
import monix.reactive.Observable

class AuthenticationManagement(implicit client: KeycloakClient) extends services.AuthenticationManagement[Task, Observable[ByteString]]

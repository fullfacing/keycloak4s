package com.fullfacing.keycloak4s.admin.monix.services

import akka.util.ByteString
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task
import monix.reactive.Observable

class ClientScopes(implicit client: KeycloakClient) extends services.ClientScopes[Task, Observable[ByteString]]

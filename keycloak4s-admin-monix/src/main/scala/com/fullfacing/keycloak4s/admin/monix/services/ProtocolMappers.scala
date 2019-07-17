package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task
import monix.reactive.Observable

class ProtocolMappers[T](implicit client: KeycloakClient[T]) extends services.ProtocolMappers[Task, Observable[T]]

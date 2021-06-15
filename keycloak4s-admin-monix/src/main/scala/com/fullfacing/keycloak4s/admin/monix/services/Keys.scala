package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task

class Keys[T](implicit client: KeycloakClient[T]) extends services.Keys[Task]

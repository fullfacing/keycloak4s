package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task

class Components[T](implicit client: KeycloakClient[T]) extends services.Components[Task]

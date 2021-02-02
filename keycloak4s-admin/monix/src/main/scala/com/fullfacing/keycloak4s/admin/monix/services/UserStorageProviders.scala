package com.fullfacing.keycloak4s.admin.monix.services

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task
import monix.reactive.Observable

class UserStorageProviders[T](implicit client: KeycloakClient[T]) extends services.UserStorageProviders[Task, Observable[T]]

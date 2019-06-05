package com.fullfacing.keycloak4s.admin.monix.services

import java.nio.ByteBuffer

import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import com.fullfacing.keycloak4s.admin.services
import monix.eval.Task
import monix.reactive.Observable

class UserStorageProviders(implicit client: KeycloakClient) extends services.UserStorageProviders[Task, Observable[ByteBuffer]]

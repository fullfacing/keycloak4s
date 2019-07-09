package com.fullfacing.keycloak4s.admin.monix.services


import java.nio.ByteBuffer

import com.fullfacing.keycloak4s.admin.services
import com.fullfacing.keycloak4s.admin.monix.client.KeycloakClient
import monix.eval.Task
import monix.reactive.Observable

class ClientScopes(implicit client: KeycloakClient) extends services.ClientScopes[Task, Observable[ByteBuffer]]

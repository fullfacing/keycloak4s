package com.fullfacing.keycloak4s.auth.akka.http.models.common

case class AuthResource(resource: String,
                        auth: List[MethodRoles])
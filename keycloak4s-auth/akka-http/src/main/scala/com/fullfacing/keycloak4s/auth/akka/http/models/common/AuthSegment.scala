package com.fullfacing.keycloak4s.auth.akka.http.models.common

case class AuthSegment(segment: String,
                       auth: List[MethodRoles])
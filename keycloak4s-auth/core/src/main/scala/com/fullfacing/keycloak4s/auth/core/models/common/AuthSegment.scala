package com.fullfacing.keycloak4s.auth.core.models.common

final case class AuthSegment(segment: String,
                             methodRoles: List[MethodRoles])
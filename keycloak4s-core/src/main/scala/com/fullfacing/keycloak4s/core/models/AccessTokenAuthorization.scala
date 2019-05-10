package com.fullfacing.keycloak4s.core.models

final case class AccessTokenAuthorization(permissions: List[Permission] = List.empty[Permission])

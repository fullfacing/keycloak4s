package com.fullfacing.keycloak4s.models

final case class AccessTokenAuthorization(permissions: List[Permission] = List.empty[Permission])

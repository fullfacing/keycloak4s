package com.fullfacing.keycloak4s.admin.models

final case class AccessTokenAuthorization(permissions: List[Permission] = List.empty[Permission])

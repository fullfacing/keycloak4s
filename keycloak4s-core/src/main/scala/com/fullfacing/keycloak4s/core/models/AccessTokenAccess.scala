package com.fullfacing.keycloak4s.core.models

final case class AccessTokenAccess(roles: Option[List[String]],
                                   verify_caller: Option[Boolean])

package com.fullfacing.keycloak4s.core.models

final case class ImpersonationResponse(sameRealm: Boolean,
                                       redirect: String)

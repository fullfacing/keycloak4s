package com.fullfacing.keycloak4s.models

final case class ImpersonationResponse(sameRealm: Boolean,
                                       redirect: String)

package com.fullfacing.keycloak4s.admin.models

final case class ImpersonationResponse(sameRealm: Boolean,
                                       redirect: String)

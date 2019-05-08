package com.fullfacing.keycloak4s.admin.models

final case class NewAuthenticationFlow(alias: String,
                                       `type`: String,
                                       provider: String,
                                       description: String)

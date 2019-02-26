package com.fullfacing.keycloak4s.models

case class NewAuthenticationFlow(alias: String,
                                 `type`: String,
                                 provider: String,
                                 description: String)

package com.fullfacing.keycloak4s.core.models

final case class AuthenticatorConfigInfo(helpText: String,
                                         name: String,
                                         properties: List[ConfigProperty],
                                         providerId: String)
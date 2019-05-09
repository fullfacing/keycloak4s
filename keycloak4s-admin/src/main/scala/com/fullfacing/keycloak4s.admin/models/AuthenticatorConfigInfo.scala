package com.fullfacing.keycloak4s.admin.models

final case class AuthenticatorConfigInfo(helpText: Option[String],
                                         name: Option[String],
                                         properties: Option[List[ConfigProperty]],
                                         providerId: Option[String])
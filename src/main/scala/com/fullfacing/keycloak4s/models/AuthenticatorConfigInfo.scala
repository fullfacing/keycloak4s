package com.fullfacing.keycloak4s.models

case class AuthenticatorConfigInfo(helpText: Option[String],
                                   name: Option[String],
                                   properties: Option[List[ConfigProperty]],
                                   providerId: Option[String])
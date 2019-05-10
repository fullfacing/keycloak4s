package com.fullfacing.keycloak4s.core.models

final case class AuthenticationExecutionExport(authenticator: Option[String],
                                               authenticatorConfig: Option[String],
                                               authenticatorFlow: Option[Boolean],
                                               autheticatorFlow: Option[Boolean],
                                               flowAlias: Option[String],
                                               priority: Option[Int],
                                               requirement: Option[String],
                                               userSetupAllowed: Option[String])

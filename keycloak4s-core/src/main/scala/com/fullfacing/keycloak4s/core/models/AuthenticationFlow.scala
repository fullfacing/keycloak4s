package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class AuthenticationFlow(alias: String,
                                    authenticationExecutions: List[AuthenticationFlow.ExecutionExport],
                                    builtIn: Boolean,
                                    description: String,
                                    id: UUID,
                                    providerId: String,
                                    topLevel: Boolean)

object AuthenticationFlow {
  final case class ExecutionExport(authenticator: Option[String],
                                   authenticatorConfig: Option[String],
                                   authenticatorFlow: Option[Boolean],
                                   autheticatorFlow: Option[Boolean],
                                   flowAlias: Option[String],
                                   priority: Option[Int],
                                   requirement: Option[String],
                                   userSetupAllowed: Option[String])

  final case class New(alias: String,
                       `type`: String,
                       provider: String,
                       description: String)
}
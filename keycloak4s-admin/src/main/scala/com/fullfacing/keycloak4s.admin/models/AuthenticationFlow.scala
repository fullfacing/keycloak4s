package com.fullfacing.keycloak4s.admin.models

import java.util.UUID

final case class AuthenticationFlow(alias: Option[String],
                                    authenticationExecutions: Option[List[AuthenticationExecutionExport]],
                                    builtIn: Option[Boolean],
                                    description: Option[String],
                                    id: Option[UUID],
                                    providerId: Option[String],
                                    topLevel: Option[Boolean])
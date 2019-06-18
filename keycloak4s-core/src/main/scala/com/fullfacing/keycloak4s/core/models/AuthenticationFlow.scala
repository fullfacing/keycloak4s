package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class AuthenticationFlow(alias: String,
                                    authenticationExecutions: List[AuthenticationExecutionExport],
                                    builtIn: Boolean,
                                    description: String,
                                    id: UUID,
                                    providerId: String,
                                    topLevel: Boolean)
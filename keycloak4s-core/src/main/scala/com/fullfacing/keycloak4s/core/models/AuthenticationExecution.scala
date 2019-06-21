package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class AuthenticationExecution(authenticator: String,
                                         authenticatorConfig: Option[UUID],
                                         authenticatorFlow: Boolean,
                                         flowId: Option[String],
                                         id: UUID,
                                         parentFlow: String,
                                         priority: Int,
                                         requirement: String,
                                         enabled: Boolean,
                                         disabled: Boolean,
                                         required: Boolean,
                                         optional: Boolean,
                                         alternative: Boolean)
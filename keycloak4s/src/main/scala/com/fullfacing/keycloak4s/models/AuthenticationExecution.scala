package com.fullfacing.keycloak4s.models

import java.util.UUID

final case class AuthenticationExecution(authenticator: Option[String],
                                         authenticatorConfig: Option[String],
                                         authenticatorFlow: Option[Boolean],
                                         flowId: Option[String],
                                         id: Option[UUID],
                                         parentFlow: Option[String],
                                         priority: Option[Int],
                                         requirement: Option[String],
                                         enabled: Option[Boolean],
                                         disabled: Option[Boolean],
                                         required: Option[Boolean],
                                         optional: Option[Boolean],
                                         alternative: Option[Boolean])
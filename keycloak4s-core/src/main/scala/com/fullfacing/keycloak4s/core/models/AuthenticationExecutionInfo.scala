package com.fullfacing.keycloak4s.core.models

import java.util.UUID

import com.fullfacing.keycloak4s.core.models.enums.Requirement

final case class AuthenticationExecutionInfo(alias: Option[String],
                                             authenticationConfig: Option[String],
                                             authenticationFlow: Option[Boolean],
                                             configurable: Boolean,
                                             displayName: String,
                                             flowId: Option[String],
                                             id: UUID,
                                             index: Int,
                                             level: Int,
                                             providerId: Option[String],
                                             requirement: Requirement,
                                             requirementChoices: List[Requirement])

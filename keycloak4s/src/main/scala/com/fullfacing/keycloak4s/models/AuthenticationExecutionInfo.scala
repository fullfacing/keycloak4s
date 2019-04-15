package com.fullfacing.keycloak4s.models

final case class AuthenticationExecutionInfo(alias: Option[String],
                                             authenticationConfig: Option[String],
                                             authenticationFlow: Option[Boolean],
                                             configurable: Option[Boolean],
                                             displayName: Option[String],
                                             flowId: Option[String],
                                             id: Option[String],
                                             index: Option[Int],
                                             level: Option[Int],
                                             providerId: Option[Int],
                                             requirement: Option[String],
                                             requirementChoices: Option[List[String]])

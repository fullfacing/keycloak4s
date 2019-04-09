package com.fullfacing.keycloak4s.models

final case class AuthenticationExecution(authenticator: Option[String],
                                         authenticatorConfig: Option[String],
                                         flowId: Option[String],
                                         id: Option[String],
                                         parentFlow: Option[String],
                                         priority: Option[Int],
                                         requirement: Option[Int])
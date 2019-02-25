package com.fullfacing.keycloak4s.models

case class AuthenticationFlow(
                               alias: Option[String],
                               authenticationExecutions: Option[List[AuthenticationExecutionExport]],
                               builtIn: Option[Boolean],
                               description: Option[String],
                               id: Option[String],
                               providerId: Option[String],
                               topLevel: Option[Boolean]
                             )
package com.fullfacing.keycloak4s.models

case class AuthenticationExecution(
                                    authenticator: Option[String],
                                    authenticatorConfig: Option[String],
                                    authenticatorFlow: Option[Boolean],
                                    autheticatorFlow: Option[Boolean],
                                    flowId: Option[String],
                                    id: Option[String],
                                    parentFlow: Option[String],
                                    priority: Option[Int],
                                    requirement: Option[Int]
                                  )
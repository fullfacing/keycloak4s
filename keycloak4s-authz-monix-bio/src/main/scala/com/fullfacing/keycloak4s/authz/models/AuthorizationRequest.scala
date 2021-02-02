package com.fullfacing.keycloak4s.authz.client.models

final case class AuthorizationRequest(ticket: Option[String] = None,
                                      claimToken: Option[String] = None,
                                      claimTokenFormat: Option[String] = None,
                                      pct: Option[String] = None,
                                      rpt: Option[String] = None,
                                      scope: Option[String] = None,
                                      audience: Option[String] = None,
                                      subjectToken: Option[String] = None)

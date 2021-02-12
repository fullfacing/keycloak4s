package com.fullfacing.keycloak4s.authz.monix.bio.models

import com.fullfacing.keycloak4s.authz.monix.bio.models.AuthorizationRequest.{Metadata, Permission}

final case class AuthorizationRequest(ticket: Option[String] = None,
                                      claimToken: Option[String] = None,
                                      claimTokenFormat: Option[String] = None,
                                      pct: Option[String] = None,
                                      rptToken: Option[String] = None,
                                      scope: Option[String] = None,
                                      audience: Option[String] = None,
                                      subjectToken: Option[String] = None,
                                      permissions: List[Permission],
                                      metadata: Option[Metadata])

object AuthorizationRequest {

  final case class Permission()

  final case class Metadata(includeResourceName: Boolean = true,
                            limit: Int,
                            responseMode: String)
}

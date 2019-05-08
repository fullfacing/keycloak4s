package com.fullfacing.keycloak4s.admin.models

final case class ScopeMapping(client: Option[String],
                              clientScope: Option[String],
                              roles: Option[List[String]],
                              self: Option[String])

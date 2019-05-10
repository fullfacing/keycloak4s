package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.PolicyType

final case class PartialImport(clients: Option[List[Client]],
                               groups: Option[List[Group]],
                               identityProviders: Option[List[IdentityProvider]],
                               ifResourceExists: Option[String],
                               policy: Option[PolicyType],
                               roles: Option[Role],
                               users: Option[List[User]])
package com.fullfacing.keycloak4s.models

final case class PartialImport(clients: Option[List[Client]],
                               groups: Option[List[Group]],
                               identityProviders: Option[List[IdentityProvider]],
                               ifResourceExists: Option[String],
                               policy: Option[String],
                               roles: Option[Role],
                               users: Option[List[User]])
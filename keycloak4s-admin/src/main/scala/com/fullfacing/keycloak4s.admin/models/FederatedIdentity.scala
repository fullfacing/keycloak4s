package com.fullfacing.keycloak4s.admin.models

final case class FederatedIdentity(identityProvider: Option[String],
                                   userId: Option[String],
                                   userName: Option[String])

package com.fullfacing.keycloak4s.models

final case class FederatedIdentity(identityProvider: Option[String],
                                   userId: Option[String],
                                   userName: Option[String])

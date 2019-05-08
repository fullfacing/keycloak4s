package com.fullfacing.keycloak4s.admin.models

final case class IdentityProviderMapper(config: Option[Map[String, String]],
                                        id: Option[String],
                                        identityProviderAlias: Option[String],
                                        identityProviderMapper: Option[String],
                                        name: Option[String])

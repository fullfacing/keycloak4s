package com.fullfacing.keycloak4s.core.models

import java.util.UUID

import com.fullfacing.keycloak4s.core.models.enums.MapperType

final case class IdentityProviderMapper(id: UUID,
                                        name: String,
                                        config: Map[String, String],
                                        identityProviderAlias: String,
                                        identityProviderMapper: MapperType)

object IdentityProviderMapper {
  final case class Create(name: String,
                          identityProviderAlias: String,
                          identityProviderMapper: MapperType,
                          config: Map[String, String] = Map.empty[String, String])

  final case class Update(id: UUID,
                          identityProviderAlias: String,
                          identityProviderMapper: MapperType,
                          config: Map[String, String])
}

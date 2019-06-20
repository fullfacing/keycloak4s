package com.fullfacing.keycloak4s.core.models

final case class IdentityProviderMapperType(id: String,
                                            name: String,
                                            category: String,
                                            helpText: String,
                                            properties: List[MapperProperties])

final case class MapperProperties(name: String,
                                  label: String,
                                  helpText: String,
                                  `type`: String,
                                  secret: Boolean)

package com.fullfacing.keycloak4s.models

final case class ProviderRepresentation(operationalInfo: Option[Map[_, _]],
                                        order: Option[Int])

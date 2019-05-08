package com.fullfacing.keycloak4s.admin.models

final case class ProviderRepresentation(operationalInfo: Option[Map[_, _]],
                                        order: Option[Int])

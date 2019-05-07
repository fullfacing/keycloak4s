package com.fullfacing.keycloak4s.admin.models

final case class AddressClaimSet(country: Option[String],
                                 formatted: Option[String],
                                 locality: Option[String],
                                 postal_code: Option[String],
                                 region: Option[String],
                                 street_address: Option[String])

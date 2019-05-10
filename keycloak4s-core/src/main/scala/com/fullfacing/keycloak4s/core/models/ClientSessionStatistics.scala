package com.fullfacing.keycloak4s.core.models

//IMPORTANT! final case class is not concrete, as it was not included in documentation and was determined from request/response testing.
final case class ClientSessionStatistics(offline: String,
                                         clientId: String,
                                         active: String,
                                         id: String)

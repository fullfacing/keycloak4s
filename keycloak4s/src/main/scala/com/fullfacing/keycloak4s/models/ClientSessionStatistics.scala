package com.fullfacing.keycloak4s.models

//IMPORTANT! Case class is not concrete, as it was not included in documentation and was determined from request/response testing.
case class ClientSessionStatistics(offline: String,
                                   clientId: String,
                                   active: String,
                                   id: String)

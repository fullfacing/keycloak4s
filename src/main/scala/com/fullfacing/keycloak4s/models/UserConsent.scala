package com.fullfacing.keycloak4s.models

case class UserConsent(clientId: Option[String],
                       createdDate: Option[Long],
                       grantedClientScopes: Option[List[String]],
                       lastUpdatedDate: Option[Long])

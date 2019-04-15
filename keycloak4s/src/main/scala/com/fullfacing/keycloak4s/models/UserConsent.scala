package com.fullfacing.keycloak4s.models

final case class UserConsent(clientId: Option[String],
                             createdDate: Option[Long],
                             grantedClientScopes: Option[List[String]],
                             lastUpdatedDate: Option[Long],
                             additionalGrants: List[OfflineTokens] = List.empty[OfflineTokens])

final case class OfflineTokens(client: String,
                               key: String)
package com.fullfacing.keycloak4s.models


/**
  * Response class determined from:
  * https://github.com/keycloak/keycloak/blob/master/services/src/main/java/org/keycloak/services/resources/admin/UserStorageProviderResource.java#L102
  */
final case class SimpleNameResponse(id: String,
                                    name: String)

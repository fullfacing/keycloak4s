package com.fullfacing.keycloak4s.authz.resources

import com.fullfacing.keycloak4s.authz.client.AuthzClient
import com.fullfacing.keycloak4s.admin.models.TokenResponse
import com.fullfacing.keycloak4s.authz.client.models.AuthorizationRequest
import com.fullfacing.keycloak4s.core.models.KeycloakError
import monix.bio.IO

final class AuthorizationResource[S](implicit client: AuthzClient[S]) {

//  def authorize(request: AuthorizationRequest): IO[KeycloakError, TokenResponse] = {
//    ???
//  }
}

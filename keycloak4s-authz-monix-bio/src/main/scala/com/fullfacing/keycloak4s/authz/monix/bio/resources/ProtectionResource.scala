package com.fullfacing.keycloak4s.authz.monix.bio.resources

import com.fullfacing.keycloak4s.authz.monix.bio.client.AuthzClient
import com.fullfacing.keycloak4s.core.models.AccessToken.Permission

final class ProtectionResource[S](client: AuthzClient[S]) {

  final case class IntrospectionResponse(active: String,
                                         permissions: List[Permission])

//  def introspectRequestingPartyToken(rpt: String): IO[KeycloakError, IntrospectionResponse] = {
//
//    uri"${client.serverConfig.introspection_endpoint}"
//    ???
//  }
}

package com.fullfacing.keycloak4s.authz.monix.bio.resources

import com.fullfacing.keycloak4s.authz.client.AuthzClient
import com.fullfacing.keycloak4s.core.models.AccessToken.Permission
import com.fullfacing.keycloak4s.core.models.KeycloakError
import monix.bio.IO
import sttp.client.UriContext

final class ProtectionResource[S](client: AuthzClient[S]) {

  final case class IntrospectionResponse(active: String,
                                         permissions: List[Permission])

//  def introspectRequestingPartyToken(rpt: String): IO[KeycloakError, IntrospectionResponse] = {
//
//    uri"${client.serverConfig.introspection_endpoint}"
//    ???
//  }
}

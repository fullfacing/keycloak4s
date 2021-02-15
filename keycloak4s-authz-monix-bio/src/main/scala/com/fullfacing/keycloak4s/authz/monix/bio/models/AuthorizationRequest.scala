package com.fullfacing.keycloak4s.authz.monix.bio.models

import com.fullfacing.keycloak4s.authz.monix.bio.models.AuthorizationRequest.Permission
import com.fullfacing.keycloak4s.core.models.KeycloakConfig.Auth

/**
 *
 * @param auth                  Optional authentication parameters used for the request, in case they differ from those used when the AuthzClient instance was created.
 * @param ticket                This parameter is optional. The most recent permission ticket received by the client as part of the UMA authorization process.
 * @param claimToken            This parameter is optional. A string representing additional claims that should be considered by the server when evaluating permissions for the resource(s) and scope(s) being requested.
 *                              This parameter allows clients to push claims to Keycloak. For more details about all supported token formats see claim_token_format parameter.
 * @param claimTokenFormat      This parameter is optional. A string indicating the format of the token specified in the claim_token parameter.
 *                              Keycloak supports two token formats: urn:ietf:params:oauth:token-type:jwt and https://openid.net/specs/openid-connect-core-1_0.html#IDToken.
 *                              The urn:ietf:params:oauth:token-type:jwt format indicates that the claim_token parameter references an access token.
 *                              The https://openid.net/specs/openid-connect-core-1_0.html#IDToken indicates that the claim_token parameter references an OpenID Connect ID Token.
 * @param pct
 * @param rptToken              This parameter is optional. A previously issued RPT which permissions should also be evaluated and added in a new one.
 *                              This parameter allows clients in possession of an RPT to perform incremental authorization where permissions are added on demand.
 * @param scope
 * @param audience              This parameter is optional. The client identifier of the resource server to which the client is seeking access. This parameter is mandatory in case the permission parameter is defined.
 *                              It serves as a hint to Keycloak to indicate the context in which permissions should be evaluated.
 * @param subjectToken
 * @param submitRequest         This parameter is optional. A boolean value indicating whether the server should create permission requests to the resources and scopes referenced by a permission ticket.
 *                              This parameter only has effect if used together with the ticket parameter as part of a UMA authorization process.
 * @param permissions           This parameter is optional. A string representing a set of one or more resources and scopes the client is seeking access.
 *                              This parameter can be defined multiple times in order to request permission for multiple resource and scopes.
 *                              This parameter is an extension to urn:ietf:params:oauth:grant-type:uma-ticket grant type in order to allow clients to send authorization requests without a permission ticket.
 * @param includeResourceName   This parameter is optional. A boolean value indicating to the server whether resource names should be included in the RPT’s permissions. If false, only the resource identifier is included.
 * @param limit                 This parameter is optional. An integer N that defines a limit for the amount of permissions an RPT can have. When used together with rpt parameter, only the last N requested permissions will be kept in the RPT.
 */
final case class AuthorizationRequest(auth: Option[Auth] = None,
                                      ticket: Option[String] = None,
                                      claimToken: Option[String] = None,
                                      claimTokenFormat: Option[String] = None,
                                      pct: Option[String] = None,
                                      rptToken: Option[String] = None,
                                      scope: Option[String] = None,
                                      audience: Option[String] = None,
                                      subjectToken: Option[String] = None,
                                      submitRequest: Option[Boolean] = None,
                                      permissions: List[Permission] = List.empty[Permission],
                                      includeResourceName: Option[Boolean] = None,
                                      limit: Option[Boolean] = None)

object AuthorizationRequest {

  final case class Permission(resourceId: String,
                              resourceName: String,
                              scopes: List[String],
                              claims: Map[String, List[String]])
}

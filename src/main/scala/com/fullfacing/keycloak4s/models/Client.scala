package com.fullfacing.keycloak4s.models

case class Client(access: Option[ClientAccess],
                  adminUrl: Option[String],
                  attributes: Option[Map[String, String]],
                  authenticationFlowBindingOverrides: Option[Map[String, AnyRef]], //Potential for stronger typing, requires example.
                  authorizationServicesEnabled: Option[Boolean],
                  authorizationSettings: Option[ResourceServer],
                  baseUrl: Option[String],
                  bearerOnly: Option[Boolean],
                  clientAuthenticatorType: Option[String],
                  clientId: Option[String],
                  consentRequired: Option[Boolean],
                  defaultClientScopes: Option[List[String]],
                  defaultRoles: Option[List[String]],
                  description: Option[String],
                  directAccessGrantsEnabled: Option[Boolean],
                  enabled: Option[Boolean],
                  frontchannelLogout: Option[Boolean],
                  fullScopeAllowed: Option[Boolean],
                  id: Option[String],
                  implicitFlowEnabled: Option[Boolean],
                  name: Option[String],
                  nodeReRegistrationTimeout: Option[Int],
                  notBefore: Option[Int],
                  optionalClientScopes: Option[List[String]],
                  origin: Option[String],
                  protocol: Option[String],
                  protocolMappers: Option[List[ProtocolMapper]],
                  publicClient: Option[Boolean],
                  redirectUris: List[String],
                  registeredNodes: List[Map[String, AnyRef]], //Potential for stronger typing, requires example.
                  registrationAccessToken: Option[String],
                  rootUrl: Option[String],
                  secret: Option[String],
                  serviceAccountsEnabled: Option[Boolean],
                  standardFlowEnabled: Option[Boolean],
                  surrogateAuthRequired: Option[Boolean],
                  webOrigins: Option[List[String]])

case class ClientAccess(view: Boolean,
                        configure: Boolean,
                        manage: Boolean)
